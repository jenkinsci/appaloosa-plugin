/*
 * The MIT License
 *
 * Copyright (c) 2011 eXo platform
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins.plugins.appaloosa;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.plugins.promoted_builds.Promotion;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.RunList;
import hudson.util.Secret;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.appaloosastore.client.AppaloosaClient;

public class AppaloosaPublisher extends Recorder {

    public final Secret token;
    public final String filePattern;
	public final String proxyHost;
    public final String proxyUser;
    public final String proxyPass;
    public final int proxyPort;
	public final String description;
	public final String groups;
	
    @DataBoundConstructor
	public AppaloosaPublisher(String token, String filePattern,
			String proxyHost, String proxyUser, String proxyPass,
			int proxyPort, String description, String groups) {
        this.token = Secret.fromString(token);
        this.filePattern = filePattern;
		this.proxyHost = proxyHost;
		this.proxyUser = proxyUser;
		this.proxyPass = proxyPass;
		this.proxyPort = proxyPort;
		this.description = description;
		this.groups = groups;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build.getResult().isWorseOrEqualTo(Result.FAILURE))
            return true; // nothing to do

        // Validates that the organization token is filled in the project configuration.
        if (StringUtils.isBlank(Secret.toString(token))) {
            listener.error(Messages._AppaloosaPublisher_noToken().toString());
            return false;
        }

        // Validates that the file pattern is filled in the project configuration.
        if (StringUtils.isBlank(filePattern)) {
            listener.error(Messages._AppaloosaPublisher_noFilePattern().toString());
            return false;
        }

        //search file in the workspace with the pattern
        FileFinder fileFinder = new FileFinder(filePattern);

        // Where we'll get artifacts from
        FilePath rootDir;
        // If the promotion plugin is used we have to take care to get data from the original build (not the promotion build)
        if (Hudson.getInstance().getPlugin("promoted-builds") != null && build instanceof Promotion) {
            rootDir = new FilePath (((Promotion) build).getTarget().getArtifactsDir());
        } else {
            rootDir = build.getWorkspace();
            if (rootDir==null) { // slave down?
                listener.error(Messages.AppaloosaPublisher_buildWorkspaceUnavailable());
                return false;
            }
        }
        listener.getLogger().println(Messages.AppaloosaPublisher_RootDirectory(rootDir));

        List<String> fileNames = rootDir.act(fileFinder);
        listener.getLogger().println(Messages.AppaloosaPublisher_foundFiles(fileNames));

        if (fileNames.isEmpty()) {
            listener.error(Messages._AppaloosaPublisher_noArtifactsFound(filePattern).toString());
            return false;
        }

        // Initialize Appaloosa Client
        AppaloosaClient appaloosaClient = new AppaloosaClient(Secret.toString(token),proxyHost,proxyPort,proxyUser,proxyPass);
        appaloosaClient.useLogger(listener.getLogger());

        boolean result=true;
        // Deploy each artifact found
        for (String filename : fileNames) {
            File tmpArchive = File.createTempFile("jenkins", "temp-appaloosa-deploy."+FilenameUtils.getExtension(filename));

            try {
                // handle remote slave case so copy binary locally
                Node buildNode = Hudson.getInstance().getNode(build.getBuiltOnStr());
                FilePath tmpLocalFile = new FilePath(tmpArchive);
                FilePath remoteFile = rootDir.child(filename);
                remoteFile.copyTo(tmpLocalFile);

				listener.getLogger().println(Messages.AppaloosaPublisher_deploying(filename, description, groups));
				appaloosaClient.deployFile(tmpArchive.getAbsolutePath(), description, groups);
                listener.getLogger().println(Messages.AppaloosaPublisher_deployed());
            } catch (Exception e) {
                listener.getLogger().println(Messages.AppaloosaPublisher_deploymentFailed(e.getMessage()));
                result=false;
            } finally {
                FileUtils.deleteQuietly(tmpArchive);
            }

        }
        return result;
    }

	@Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        ArrayList<AppaloosaBuildAction> actions = new ArrayList<AppaloosaBuildAction>();
        RunList<? extends AbstractBuild<?, ?>> builds = project.getBuilds();

        Collection predicated = CollectionUtils.select(builds, new Predicate() {
            public boolean evaluate(Object o) {
                Result r = ((AbstractBuild<?, ?>) o).getResult();
                return r!=null && r.isBetterOrEqualTo(Result.SUCCESS);
            }
        });

        ArrayList<AbstractBuild<?, ?>> filteredList = new ArrayList<AbstractBuild<?, ?>>(predicated);


        Collections.reverse(filteredList);
        for (AbstractBuild<?, ?> build : filteredList) {
            List<AppaloosaBuildAction> appaloosaActions = build.getActions(AppaloosaBuildAction.class);
            if (appaloosaActions != null && appaloosaActions.size() > 0) {
                for (AppaloosaBuildAction action : appaloosaActions) {
                    actions.add(new AppaloosaBuildAction(action));
                }
                break;
            }
        }

        return actions;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(AppaloosaPublisher.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            // XXX is this now the right style?
            req.bindJSON(this, json);
            save();
            return true;
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public FormValidation doCheckFilePattern(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(),value);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return Messages.AppaloosaPublisher_uploadToAppaloosa();
        }
    }
}
