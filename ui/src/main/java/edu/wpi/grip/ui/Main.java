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
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.inject.Inject;

public class Main extends Application {

    @Inject private EventBus eventBus;
    @Inject private Palette palette;

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
    public void start(Stage stage) throws Exception {
        root = FXMLLoader.load(Main.class.getResource("MainWindow.fxml"), null, null, injector::getInstance);
        root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);

        stage.setTitle("GRIP Computer Vision Engine");
        stage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Subscribe
    public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
        // Print throwable before showing the exception so that errors are in order in the console
        event.getThrowable().printStackTrace();
        PlatformImpl.runAndWait(() -> {
            synchronized (this.dialogLock) {
                try {
                    // Don't create more than one exception dialog at the same time
                    final ExceptionAlert exceptionAlert = new ExceptionAlert(root, event.getThrowable(), event.getMessage(), event.isFatal(), getHostServices());
                    exceptionAlert.setInitialFocus();
                    exceptionAlert.showAndWait();
                } catch (Throwable e) {
                    // Well in this case something has gone very, very wrong
                    // We don't want to create a feedback loop either.
                    e.printStackTrace();
                    System.exit(1); // Ensure we shut down the application if we get an exception
                }
            }
        });

        if (event.isFatal()) {
            System.err.println("Original fatal exception");
            event.getThrowable().printStackTrace();
            System.exit(1);
        }
    }
}
