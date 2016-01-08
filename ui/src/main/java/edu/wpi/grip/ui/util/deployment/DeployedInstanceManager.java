package edu.wpi.grip.ui.util.deployment;


import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcabi.ssh.Shell;
import com.jcraft.jsch.JSch;
import edu.wpi.grip.core.StartStoppable;
import edu.wpi.grip.core.events.StartedStoppedEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.serialization.Project;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.jdeferred.DeferredCallable;
import org.jdeferred.DeferredManager;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls an instance of GRIP running on a remote device.
 */
public class DeployedInstanceManager implements StartStoppable {
    private static final String DEPLOYED_GRIP_FILE_NAME = "grip.jar";

    private final Logger logger = Logger.getLogger(DeployedInstanceManager.class.getName());
    private final EventBus eventBus;
    private final File coreJar;
    private final File projectFile;
    private final SecureShellDetails details;
    private final DeploymentCommands deploymentCommands;
    private final Supplier<OutputStream> stdOut;
    private final Supplier<OutputStream> stdErr;
    private Optional<Thread> sshThread;

    @Singleton
    public static class Factory {
        private final EventBus eventBus;
        private final File coreJAR;
        private final Project project;
        private final SecureShellDetails.Factory secureShellDetailsFactory;
        private final DeploymentCommands.Factory deploymentCommandsFactory;

        @Inject
        public Factory(
                EventBus eventBus,
                Project project,
                SecureShellDetails.Factory secureShellDetailsFactory,
                DeploymentCommands.Factory deploymentCommandsFactory) {
            this.eventBus = eventBus;
            try {
                final File coreJarInSource = new File(edu.wpi.grip.core.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                final File newJarInTempDir = new File(new File(System.getProperty("java.io.tmpdir")), DEPLOYED_GRIP_FILE_NAME);
                Files.copy(coreJarInSource.toPath(), newJarInTempDir.toPath());
                this.coreJAR = newJarInTempDir;
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Could not find the main class jar file", e);
            } catch (IOException e) {
                throw new IllegalStateException("Could not copy core jar to temporary directory", e);
            }
            this.project = project;
            this.secureShellDetailsFactory = secureShellDetailsFactory;
            this.deploymentCommandsFactory = deploymentCommandsFactory;
        }

        public DeployedInstanceManager createFRC(InetAddress address) {
            return createFRC(address, NullOutputStream::new, NullOutputStream::new);
        }

        public DeployedInstanceManager createFRC(InetAddress address, Supplier<OutputStream> stdOut, Supplier<OutputStream> stdErr) {
            final File projectFile = project.getFile().get();
            return createFRC(address, projectFile, stdOut, stdErr);
        }

        public DeployedInstanceManager createFRC(InetAddress address, File projectFile) {
            return createFRC(address, projectFile, NullOutputStream::new, NullOutputStream::new);
        }

        public DeployedInstanceManager createFRC(InetAddress addresses, File projectFile, Supplier<OutputStream> stdOut, Supplier<OutputStream> stdErr) {
            return new DeployedInstanceManager(eventBus, coreJAR, projectFile, secureShellDetailsFactory.createFRC(addresses), deploymentCommandsFactory.createFRC(), stdOut, stdErr);
        }
    }

    /**
     * @param eventBus
     * @param coreJar            The jar with all of the core. This will be copied to the destination
     * @param projectFile        The project file to send over
     * @param details            The details regarding connecting to the secure shell
     * @param deploymentCommands The commands required to start and stop GRIP
     * @param stdOut             Supplies the stream to be used for the standard output from the ssh command
     * @param stdErr             Supplies the stream to be used for the standard error from the ssh command
     */
    private DeployedInstanceManager(EventBus eventBus,
                                   File coreJar,
                                   File projectFile,
                                   SecureShellDetails details,
                                   DeploymentCommands deploymentCommands,
                                   Supplier<OutputStream> stdOut,
                                   Supplier<OutputStream> stdErr) {
        this.eventBus = checkNotNull(eventBus, "The event bus can not be null");
        this.coreJar = checkNotNull(coreJar, "The URI of the coreJar can not be null");
        this.projectFile = checkNotNull(projectFile, "The project file can not be null");
        this.details = checkNotNull(details, "The details can not be null");
        this.deploymentCommands = checkNotNull(deploymentCommands, "The deployment commands can not be null");
        this.stdOut = checkNotNull(stdOut, "The standard out stream supplier can not be null");
        this.stdErr = checkNotNull(stdErr, "The standard err stream supplier can not be null");
        sshThread = Optional.empty();
    }

    /**
     * Deploys the coreJar and the project file to the remote device
     *
     */
    public synchronized Promise<DeployedInstanceManager, Throwable, Double> deploy() {
        final DeployedInstanceManager self = this;
        JSch.setConfig("StrictHostKeyChecking", "no");
        final DeferredManager deferred = new DefaultDeferredManager();

        return deferred.when(new DeferredCallable<DeployedInstanceManager, Double>() {
            @Override
            public DeployedInstanceManager call() throws IOException {
                notify(0.2);
                scpFileToTarget(coreJar);
                notify(0.5);
                scpFileToTarget(projectFile);
                notify(1.0);
                return self;
            }

        });
    }

    /**
     * @param file The file to send
     * @throws IOException If there is a problem sending the file.
     */
    private void scpFileToTarget(File file) throws IOException {
        final String localFile = URLDecoder.decode(Paths.get(file.toURI()).toString());
        try {
            final Scp scp = details.createSCPRunner();
            scp.setLocalFile(localFile);
            scp.execute();
        } catch (BuildException e) {
            throw new IOException("Failed to deploy", e);
        }
    }

    /**
     * Starts GRIP running on the device specified by the secure shell details
     *
     * @throws IOException
     */
    public synchronized void start() throws IOException {
        if (isStarted()) {
            throw new IllegalStateException("The program has already been started and must be stopped before restarting");
        }
        // Ensure that the project isn't running from a previous instance.
        runStop();
        Thread launcher = new Thread(() -> {
            try {
                final Shell gripShell = new Shell.Safe(details.createSSHShell());
                gripShell.exec("nohup " + deploymentCommands.getJARLaunchCommand(coreJar.getName(), projectFile.getName()) + " &",
                        new NullInputStream(0L),
                        stdOut.get(),
                        stdErr.get());
            } catch (IOException e) {
                throw new IllegalStateException("The program failed to start", e);
            } finally {
                // This thread is done, shut it down.
                synchronized (this) {
                    sshThread = Optional.empty();
                }
                eventBus.post(new StartedStoppedEvent(this));
            }
        }, "SSH Monitor Thread");
        launcher.setUncaughtExceptionHandler((thread, exception) -> {
            eventBus.post(new UnexpectedThrowableEvent(exception, "Failed to start the remote instance of the application"));
            try {
                runStop();
            } catch (IOException e) {
                eventBus.post(new UnexpectedThrowableEvent(e, "Failed to stop the remote instance of the program"));
            }
        });
        launcher.setDaemon(true);
        launcher.start();
        this.sshThread = Optional.of(launcher);
        eventBus.post(new StartedStoppedEvent(this));
    }

    /**
     * Stops the program running on the remote device
     *
     * @throws IOException If the command fails to be delivered
     */
    public synchronized void stop() throws IOException {
        if (!isStarted()) {
            throw new IllegalStateException("The program hasn't started yet.");
        }
        runStop();
        do {
            try {
                // Since we hold the mutex on this we can wait
                wait(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.WARNING, "Wait interrupted", e);
            }
            runStop();
        } while (isStarted() && !Thread.interrupted());
    }

    /**
     * Makes an SSH connection to the remote and runs the kill command.
     * Should block until the command has executed.
     * @throws IOException If the shell fails.
     */
    private void runStop() throws IOException {
        final Shell.Plain gripShell = new Shell.Plain(new Shell.Safe(details.createSSHShell()));
        gripShell.exec(deploymentCommands.getKillCommand(coreJar.getName()));
    }

    @Override
    public synchronized boolean isStarted() {
        return sshThread.isPresent() && sshThread.get().isAlive();
    }

}
