package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.GRIPCoreModule;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.util.SafeShutdown;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.util.DPIUtility;
import edu.wpi.grip.ui.util.GRIPPlatform;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Inject
    private EventBus eventBus;
    @Inject
    private Palette palette;
    @Inject
    private Logger logger;
    @Inject
    private GRIPPlatform platform;
    @Inject
    private Operations operations;

    protected final Injector injector = Guice.createInjector(new GRIPCoreModule(), new GRIPUIModule());

    private final Object dialogLock = new Object();
    private Parent root;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX insists on creating the main application with its own reflection code, so we can't create with the
     * Guice and do automatic field injection. However, we can inject it after the fact.
     */
    public Main() {
        injector.injectMembers(this);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void start(Stage stage) throws Exception {
        root = FXMLLoader.load(Main.class.getResource("MainWindow.fxml"), null, null, injector::getInstance);
        root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

        operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);

        stage.setOnCloseRequest((event) -> {
            // If this isn't here this can cause a deadlock on windows
            // See issue #297
            SafeShutdown.exit(0, Platform::exit);
        });

        stage.setTitle("GRIP Computer Vision Engine");
        stage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
        stage.setScene(new Scene(root));
        stage.show();

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
