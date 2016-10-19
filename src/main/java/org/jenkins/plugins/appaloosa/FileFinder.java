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

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileFinder implements FilePath.FileCallable<List<String>> {

    private final String pattern;

    public FileFinder(final String pattern) {
        this.pattern = pattern;
    }

    public List<String> invoke(File directory, VirtualChannel channel) throws IOException, InterruptedException {
        return find(directory);
    }

    public List<String> find(final File directory)  {
        try {
            FileSet fileSet = new FileSet();
            Project antProject = new Project();
            fileSet.setProject(antProject);
            fileSet.setDir(directory);
            fileSet.setIncludes(pattern);

            String[] files = fileSet.getDirectoryScanner(antProject).getIncludedFiles();
            return files == null ? Collections.<String>emptyList() : Arrays.asList(files);
        }
        catch (BuildException exception) {
            return Collections.emptyList();
        }
    }
}
