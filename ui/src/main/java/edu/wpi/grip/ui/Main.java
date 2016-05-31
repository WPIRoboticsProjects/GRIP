package edu.wpi.grip.ui;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.GRIPCoreModule;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.network.GRIPNetworkModule;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.sources.GRIPSourcesHardwareModule;
import edu.wpi.grip.core.util.SafeShutdown;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Inject private EventBus eventBus;
    @Inject private PipelineRunner pipelineRunner;
    @Inject private Project project;
    @Inject private Operations operations;
    @Inject private CVOperations cvOperations;
    @Inject private Logger logger;

    /**
     * JavaFX insists on creating the main application with its own reflection code, so we can't create with the
     * Guice and do automatic field injection. However, we can inject it after the fact.
     */
    @VisibleForTesting
    protected Injector injector;

    private final Object dialogLock = new Object();
    private Parent root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void start(Stage stage) throws Exception {
        List<String> parameters = new ArrayList<>(getParameters().getRaw());

        if (parameters.contains("--headless")) {
            // If --headless was specified on the command line, run in headless mode (only use the core module)
            injector = Guice.createInjector(new GRIPCoreModule(), new GRIPNetworkModule(), new GRIPSourcesHardwareModule());
            injector.injectMembers(this);

            parameters.remove("--headless");
        } else {
            // Otherwise, run with both the core and UI modules, and show the JavaFX stage
            injector = Guice.createInjector(Modules.override(new GRIPCoreModule(), new GRIPNetworkModule(), new GRIPSourcesHardwareModule()).with(new GRIPUIModule()));
            injector.injectMembers(this);

            System.setProperty("prism.lcdtext", "false");
            Font.loadFont(this.getClass().getResource("roboto/Roboto-Regular.ttf").openStream(), -1);
            Font.loadFont(this.getClass().getResource("roboto/Roboto-Bold.ttf").openStream(), -1);
            Font.loadFont(this.getClass().getResource("roboto/Roboto-Italic.ttf").openStream(), -1);
            Font.loadFont(this.getClass().getResource("roboto/Roboto-BoldItalic.ttf").openStream(), -1);

            root = FXMLLoader.load(Main.class.getResource("MainWindow.fxml"), null, null, injector::getInstance);
            root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

            // If this isn't here this can cause a deadlock on windows. See issue #297
            stage.setOnCloseRequest(event -> SafeShutdown.exit(0, Platform::exit));
            stage.setTitle("GRIP Computer Vision Engine");
            stage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
            stage.setScene(new Scene(root));
            stage.show();
        }

        operations.addOperations();
        cvOperations.addOperations();

        // If there was a file specified on the command line, open it immediately
        if (!parameters.isEmpty()) {
            try {
                project.open(new File(parameters.get(0)));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error loading file: " + parameters.get(0));
                throw e;
            }
        }

        pipelineRunner.startAsync();
    }

    public void stop() {
        SafeShutdown.flagStopping();
    }

    @Subscribe
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
        event.handleSafely((throwable, message, isFatal) -> {
            // Check this so we can avoid entering the the platform wait
            // if the program is shutting down.
            if (!SafeShutdown.isStopping()) {
                // This should still use PlatformImpl
                PlatformImpl.runAndWait(() -> {
                    // WARNING! Do not post any events from within this! It could result in a deadlock!
                    synchronized (this.dialogLock) {
                        // Check again because the value could have been changed while waiting for the javafx thread to run.
                        if (!SafeShutdown.isStopping()) {
                            try {
                                // Don't create more than one exception dialog at the same time
                                final ExceptionAlert exceptionAlert = new ExceptionAlert(root, throwable, message, isFatal, getHostServices());
                                exceptionAlert.setInitialFocus();
                                exceptionAlert.showAndWait();
                            } catch (Throwable e) {
                                // Well in this case something has gone very, very wrong
                                // We don't want to create a feedback loop either.
                                try {
                                    logger.log(Level.SEVERE, "Failed to show exception alert", e);
                                } finally {
                                    SafeShutdown.exit(1); // Ensure we shut down the application if we get an exception
                                }
                            }
                        }
                    }
                });
            } else {
                logger.log(Level.INFO, "Did not display exception because UI was stopping", throwable);
            }
        });
    }
}
