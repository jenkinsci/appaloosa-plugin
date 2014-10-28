/*
 * The MIT License
 *
 * Copyright (c) 2012 eXo platform
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

import java.io.IOException;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.JobProperty;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotedProjectAction;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.BuildStep;

/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
@Extension(optional = true)
public class AppaloosaRunListener extends RunListener<AbstractBuild> {

    public void onCompleted(AbstractBuild r, TaskListener listener) {
        // Searching for the promotion plugin
        if (r.getProject().getProperty(JobPropertyImpl.class) != null) {
            JobProperty promotionJobProperty = r.getProject().getProperty(JobPropertyImpl.class);
            // Search in promotions if one of them is using Appaloosa
            for (PromotionProcess process : ((PromotedProjectAction) promotionJobProperty.getProjectAction(r.getProject())).getProcesses()) {
                // Search for a build step using Appaloosa
                for (BuildStep buildStep : process.getBuildSteps()) {
                    if (buildStep instanceof AppaloosaPublisher) {
                        listener.getLogger().println(Messages.AppaloosaRunListener_AppaloosaInPromotion(((AppaloosaPublisher) buildStep).filePattern));
                        // If appaloosa is activated in a promotion it may be needed to deploy binaries that
                        // aren't the latest in the workspace.
                        // Thus we will silently archive required artifacts to use them.
                        try {
                            // TODO : Do we need avoid to archive something already done by the job itself ?
                            // It is from POV complex to check if all artifacts deployed by appaloosa are
                            // saved by the archiver
                            //if (r.getProject().getPublishersList().get(ArtifactArchiver.class) == null) {
                            //}
                            new ArtifactArchiver(((AppaloosaPublisher) buildStep).filePattern, null, true).perform(r, null, (BuildListener) listener);
                        } catch (InterruptedException e) {
                            listener.error(Messages.AppaloosaRunListener_BackupError(), e);
                        } catch (IOException e) {
							// TODO Auto-generated catch block
                        	listener.error(Messages.AppaloosaRunListener_BackupError(), e);
						}
                    }
                }
            }
        }
    }

}
