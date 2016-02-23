package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;
import com.google.common.io.LineReader;
import com.google.common.io.Resources;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.events.StopPipelineEvent;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.settings.SettingsProvider;
import edu.wpi.grip.ui.components.LogTextArea;
import edu.wpi.grip.ui.util.StringInMemoryFile;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LoggingTransferListener;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX controller for the deploy tool.
 * <p>
 * This basically uploads a headless version of GRIP and the current project to a remote target using SSH, then
 * runs it remotely.  The default values for all fields are based on typical settings for FRC, with the address
 * based on the project settings.
 */
public class DeployController {
    private final static String GRIP_JAR = "grip.jar";
    private final static String GRIP_WRAPPER = "grip";
    private final static URL LOCAL_GRIP_URL = Project.class.getProtectionDomain().getCodeSource().getLocation();
    private final static String LOCAL_GRIP_PATH = URLDecoder.decode(LOCAL_GRIP_URL.getPath());

    @FXML
    private TextField address;
    @FXML
    private TextField user;
    @FXML
    private TextField password;
    @FXML
    private TextField javaHome;
    @FXML
    private TextField jvmArgs;
    @FXML
    private TextField deployDir;
    @FXML
    private TextField projectFile;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Label status;
    @FXML
    private LogTextArea console;
    @FXML
    private BooleanProperty deploying;
    @FXML
    private StringProperty command;
    @FXML
    private ToggleButton scrollPauseButton;

    @Inject
    private EventBus eventBus;
    @Inject
    private Project project;
    @Inject
    private SettingsProvider settingsProvider;
    @Inject
    private Logger logger;

    private Optional<Thread> deployThread = Optional.empty();

    @FXML
    public void initialize() {
        deploying.addListener((o, b, d) -> progress.setProgress(d ? ProgressIndicator.INDETERMINATE_PROGRESS : 0));
        command.bind(Bindings.concat(javaHome.textProperty(), "/bin/java ", jvmArgs.textProperty(), " -jar '",
                deployDir.textProperty(), "/", GRIP_JAR, "' '", deployDir.textProperty(), "/", projectFile.textProperty(), "'"));

        scrollPauseButton.selectedProperty().bindBidirectional(console.pausedScrollProperty());
        console.setOnScroll(event -> {
            console.setPausedScroll(true);
        });
        loadSettings(settingsProvider.getProjectSettings());
    }

    @Subscribe
    public void onSettingsChanged(ProjectSettingsChangedEvent event) {
        Platform.runLater(() -> loadSettings(event.getProjectSettings()));
    }

    private void loadSettings(ProjectSettings settings) {
        // Almost all of the deploy settings can be persistently saved in the project settings.  Whenever the project
        // settings are updated (either the user has edited them or a new project has been opened), we should update
        // the fields with the new setting values.
        address.setText(settings.getDeployAddress());
        user.setText(settings.getDeployUser());
        javaHome.setText(settings.getDeployJavaHome());
        jvmArgs.setText(settings.getDeployJvmOptions());
        deployDir.setText(settings.getDeployDir());
    }

    private void saveSettings() {
        // If the settings are updated in the deploy dialog, we still want to save them in the persistent project
        // settings, so they don't get reset the next time the project is opened to the settings are edited.
        final ProjectSettings settings = settingsProvider.getProjectSettings();
        settings.setDeployAddress(address.getText());
        settings.setDeployUser(user.getText());
        settings.setDeployJavaHome(javaHome.getText());
        settings.setDeployJvmOptions(jvmArgs.getText());
        settings.setDeployDir(deployDir.getText());
        eventBus.post(new ProjectSettingsChangedEvent(settings));
    }

    @FXML
    public void onDeploy() {
        saveSettings();

        deploying.setValue(true);
        console.clear();

        // Start the deploy in a new thread, so the GUI doesn't freeze
        deployThread = Optional.of(new Thread(this::deploy, "Deploy"));
        deployThread.get().setDaemon(true);
        deployThread.get().start();
    }

    @FXML
    public void onStop() {
        deployThread.ifPresent(Thread::interrupt);
        deploying.setValue(false);
        status.setText("");
    }

    /**
     * Upload and run the GRIP project using the current deploy settings.  This is run in a separate thread, and it
     * periodically updates the GUI to inform the user of the current status of the deployment.
     */
    private void deploy() {
        setStatusAsync("Connecting to " + address.getText(), false);

        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier((hostname, port, key) -> true);
            ssh.connect(address.getText());
            ssh.authPassword(user.getText(), password.getText());

            // Update the progress bar and status text while uploading files
            SCPFileTransfer scp = ssh.newSCPFileTransfer();
            scp.setTransferListener(new LoggingTransferListener() {
                @Override
                public StreamCopier.Listener file(String name, long size) {
                    setStatusAsync("Uploading " + name, false);
                    return transferred -> {
                        if (isNotCanceled())
                            Platform.runLater(() -> progress.setProgress((double) transferred / size));
                    };
                }
            });

            // The project may or may not be saved to a file (and even if it was saved, it might be modified), so we
            // serialize it to a string before deploying.
            StringWriter projectWriter = new StringWriter();
            project.save(projectWriter);

            final String commandStr = command.get();
            final String pathStr = deployDir.getText() + "/";


            // Upload the core GRIP JAR only if there isn't already one with the same hash on the robot.  This prevents
            // us from redundantly deploying the same JAR over and over again (so deploy times after the first are
            // much faster), while still ensuring that the JAR is deployed if it has to be.
            try (Session session = ssh.startSession()) { // SSH doesn't store a reference to the session to close it.
                final Session.Command md5Cmd = session.exec("md5sum " + pathStr + GRIP_JAR);
                final String remoteMd5Sum;
                try (DataInputStream stream = new DataInputStream(md5Cmd.getInputStream())) {
                    remoteMd5Sum = stream.readLine();
                }
                String localMd5Sum = Resources.asByteSource(LOCAL_GRIP_URL).hash(Hashing.md5()).toString();

                // If the MD5 sum doesn't match up or there's any kind of error (there isn't a JAR there, etc...),
                // just upload the JAR.
                if (remoteMd5Sum == null || remoteMd5Sum.length() < 32 || !remoteMd5Sum.substring(0, 32).equals(localMd5Sum)) {
                    scp.upload(new FileSystemFile(LOCAL_GRIP_PATH), pathStr + GRIP_JAR);
                } else {
                    logger.info("md5sum " + GRIP_JAR + " matches. Skipping upload.");
                }
            }

            // Upload the project file and a wrapper script with the JVM arguments.  These are very small and change
            // often, so we might as well upload them every time.
            scp.upload(new StringInMemoryFile(GRIP_WRAPPER,
                    "PID=$(ps aux | grep " + GRIP_JAR + "| grep -v grep | awk '{print $1}')\n"
                            + "if [ $PID ]; then kill -9 $PID; fi\n"
                            + "echo \"" + commandStr + "\"\n"
                            + commandStr, 0755), pathStr);
            scp.upload(new StringInMemoryFile(projectFile.getText(), projectWriter.toString()), pathStr);

            // Stop the pipeline before running it remotely, so the two instances of GRIP don't try to publish to the
            // same NetworkTables keys.
            eventBus.post(new StopPipelineEvent());

            // Run the project!
            setStatusAsync("Running GRIP", false);
            logger.info("Running " + commandStr);

            try (Session session = ssh.startSession()) {
                session.allocateDefaultPTY();
                Session.Command cmd = session.exec(String.format("'%s/%s'", pathStr, GRIP_WRAPPER));

                try (final InputStreamReader reader = new InputStreamReader(cmd.getInputStream(), StandardCharsets.UTF_8)) {
                    final LineReader inputReader = new LineReader(reader);
                    while (isNotCanceled()) {
                        String line = inputReader.readLine();
                        if (line == null) {
                            return;
                        }

                        Platform.runLater(() -> console.addLineToLog(line));
                    }
                }
            }
        } catch (UnknownHostException e) {
            setStatusAsync("Unknown host: " + address.getText(), true);
        } catch (UserAuthException e) {
            setStatusAsync("Invalid username or password (should be \"lvuser\" and blank for roboRIO)", true);
        } catch (InterruptedIOException e) {
            logger.info("Deploy canceled");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unexpected error deploying", e);
            setStatusAsync(e.getMessage(), true);
        } finally {
            if (isNotCanceled()) {
                Platform.runLater(() -> deploying.setValue(false));
            }
        }
    }

    /**
     * Called in the deploy thread to check if the deploy has been canceled.  This prevents the thread from continuing
     * to run commands and updating UI elements after the cancel button has been pressed, but without the need for
     * the GUI thread to join on it (and therefore block)
     */
    private boolean isNotCanceled() {
        return !Thread.currentThread().isInterrupted();
    }

    /**
     * Show a message, asynchronously in the GUI thread.  This is called periodically by the deploy thread to provide
     * feedback without locking up the GUI.  This does nothing if the current deploy has been canceled.
     */
    private void setStatusAsync(String statusText, boolean error) {
        if (isNotCanceled()) {
            logger.log(error ? Level.WARNING : Level.INFO, statusText);
            Platform.runLater(() -> {
                status.getStyleClass().setAll("label", error ? "error-label" : "info-label");
                status.setText(statusText);
            });
        }
    }
}
