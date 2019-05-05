package org.pitest.pitclipse.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.pitest.pitclipse.runner.config.PitConfiguration;
import org.pitest.pitclipse.runner.config.PitExecutionModeVisitor;

import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;

public class PitLaunchVisitor implements PitExecutionModeVisitor<Void> {

    private final ILaunchConfiguration configuration;
    private final ILaunch launch;
    private final IProgressMonitor monitor;
    private final PitConfiguration pitConfiguration;

    public PitLaunchVisitor(PitConfiguration pitConfiguration,
            ILaunchConfiguration launchConfig, ILaunch launch,
            IProgressMonitor monitor) {
        this.pitConfiguration = pitConfiguration;
        configuration = launchConfig;
        this.launch = launch;
        this.monitor = monitor;
    }

    public Void visitProjectLevelConfiguration() {
        try {
            new ProjectLevelLaunchDelegate(pitConfiguration).launch(
                    configuration, RUN_MODE, launch, monitor);
        } catch (CoreException e) {
            throw new LaunchFailedException(configuration.getName());
        }
        return null;
    }

    public Void visitWorkspaceLevelConfiguration() {
        try {
            new WorkspaceLevelLaunchDelegate(pitConfiguration).launch(
                    configuration, RUN_MODE, launch, monitor);
        } catch (CoreException e) {
            throw new LaunchFailedException(configuration.getName());
        }
        return null;
    }

    public static class LaunchFailedException extends RuntimeException {
        private static final long serialVersionUID = -1678956151196198597L;

        public LaunchFailedException(String name) {
            super(name);
        }
    }
}