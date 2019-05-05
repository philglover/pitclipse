package org.pitest.pitclipse.launch.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import java.io.File;
import java.net.URI;
import java.util.List;

public class ProjectLevelSourceDirFinder implements SourceDirFinder {

    @Override
    public List<File> getSourceDirs(LaunchConfigurationWrapper configurationWrapper) throws CoreException {
        Builder<File> sourceDirBuilder = ImmutableSet.builder();
        IJavaProject javaProject = configurationWrapper.getProject();
        URI location = getProjectLocation(javaProject.getProject());
        IPackageFragmentRoot[] packageRoots = javaProject.getPackageFragmentRoots();

        File projectRoot = new File(location);
        for (IPackageFragmentRoot packageRoot : packageRoots) {
            if (isValid(packageRoot)) {
                File packagePath = removeProjectFromPackagePath(javaProject, packageRoot.getPath());
                File potentialSourceRoot = new File(projectRoot, packagePath.toString());
                if (isValidSourceDir(potentialSourceRoot)) {
                    sourceDirBuilder.add(potentialSourceRoot);
                }
            }
        }
        return ImmutableList.copyOf(sourceDirBuilder.build());
    }

    private boolean isValid(IPackageFragmentRoot packageRoot) {
        return !packageRoot.isArchive() && !packageRoot.isExternal();
    }

    private boolean isValidSourceDir(File sourceDir) {
        return sourceDir.exists() && sourceDir.isDirectory();
    }
    
    private URI getProjectLocation(IProject project) throws CoreException {
        URI locationUri = project.getDescription().getLocationURI();
        if (null != locationUri) {
            return locationUri;
        }
        // We're using the default location under workspace
        File projLocation = new File(project.getLocation().toOSString());
        return projLocation.toURI();
    }

    private File removeProjectFromPackagePath(IJavaProject javaProject, IPath packagePath) {
        IPath newPath = packagePath.removeFirstSegments(1);
        return newPath.toFile();
    }

}
