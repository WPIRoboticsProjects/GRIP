package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.LineReader;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.events.StopPipelineEvent;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.ui.util.StringInMemoryFile;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LoggingTransferListener;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
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

    // Default deploy information for FRC. This can be overridden by the user for applications outside of FRC or not
    // using the roboRIO (ie: coprocessors)
    public final static String DEFAULT_USER = "lvuser";
    public final static String DEFAULT_JAVA_HOME = "/usr/local/frc/JRE/";
    public final static String DEFAULT_DIR = "/home/" + DEFAULT_USER;

    private final static String GRIP_JAR = "grip.jar";
    private final static String PROJECT_FILE = "project.grip";

    @FXML private TextField address;
    @FXML private TextField user;
    @FXML private TextField password;
    @FXML private TextField javaHome;
    @FXML private TextField deployDir;
    @FXML private ProgressIndicator progress;
    @FXML private Button deployButton;
    @FXML private Label status;
    @FXML private TextArea console;

    @Inject private EventBus eventBus;
    @Inject private Project project;
    @Inject private Pipeline pipeline;
    @Inject private Logger logger;

    private final BooleanProperty deploying = new SimpleBooleanProperty(this, "deploying", false);
    private Optional<Thread> deployThread = Optional.empty();

    @FXML
    public void initialize() {
        address.setText(pipeline.getProjectSettings().computeDeployAddress());
        deployButton.disableProperty().bind(deploying);
        progress.disableProperty().bind(deploying.not());
        deploying.addListener((o, b, d) -> progress.setProgress(d ? ProgressIndicator.INDETERMINATE_PROGRESS : 0));
    }

    @Subscribe
    public void updateSettings(ProjectSettingsChangedEvent event) {
        Platform.runLater(() -> address.setText(event.getProjectSettings().computeDeployAddress()));
    }

    @FXML
    public void onDeploy() {
        deploying.setValue(true);
        console.clear();

        // Start the deploy in a new thread, so the GUI doesn't freeze
        deployThread = Optional.of(new Thread(() ->
                deploy(address.getText(), user.getText(), password.getText(), javaHome.getText(), deployDir.getText())));
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
    private void deploy(String address, String user, String password, String javaHome, String deployDir) {
        setStatusAsync("Connecting to " + address, false);

        try (SSHClient ssh = new SSHClient()) {
            ssh.loadKnownHosts();
            ssh.connect(address);
            ssh.authPassword(user, password);

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

            // Upload the GRIP core JAR and the serialized project to the robot
            scp.upload(new StringInMemoryFile(PROJECT_FILE, projectWriter.toString()), deployDir + "/");
            scp.upload(new FileSystemFile(Project.class.getProtectionDomain().getCodeSource().getLocation().getPath()),
                    deployDir + "/" + GRIP_JAR);

            // Stop the pipeline before running it remotely, so the two instances of GRIP don't try to publish to the
            // same NetworkTables keys.
            eventBus.post(new StopPipelineEvent());

            // Run the project!
            setStatusAsync("Running GRIP", false);
            Session session = ssh.startSession();
            session.allocateDefaultPTY();
            Session.Command cmd = session.exec(javaHome + "/bin/java -jar " + deployDir + "/" + GRIP_JAR + " " + deployDir + "/" + PROJECT_FILE);

            LineReader inputReader = new LineReader(new InputStreamReader(cmd.getInputStream()));
            while (isNotCanceled()) {
                String line = inputReader.readLine();
                if (line == null) {
                    // Headless GRIP normally doesn't close stdout.  If that happens, check stderr for a message, since
                    // there must have been a problem.
                    setStatusAsync(IOUtils.readFully(cmd.getErrorStream()).toString(), true);
                    return;
                }

                Platform.runLater(() -> console.setText(console.getText() + line + "\n"));
            }
        } catch (UnknownHostException e) {
            setStatusAsync("Unknown host: " + address, true);
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
