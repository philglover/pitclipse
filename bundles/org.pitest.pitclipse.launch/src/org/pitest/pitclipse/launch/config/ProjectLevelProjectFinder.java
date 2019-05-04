package org.pitest.pitclipse.core.launch.config;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.pitest.pitclipse.reloc.guava.collect.ImmutableList;

import java.util.List;

public class ProjectLevelProjectFinder implements ProjectFinder {

    @Override
    public List<String> getProjects(LaunchConfigurationWrapper configurationWrapper) throws CoreException {
        IJavaProject project = configurationWrapper.getProject();
        return ImmutableList.of(project.getProject().getName());
    }

}
