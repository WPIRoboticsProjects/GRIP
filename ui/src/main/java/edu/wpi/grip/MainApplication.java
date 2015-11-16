package edu.wpi.grip;

import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.FatalErrorEvent;
import edu.wpi.grip.ui.ExceptionAlert;
import edu.wpi.grip.ui.MainWindowView;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class MainApplication extends Application {
    private static final Main.Core core = new Main().createNewCore();


    private final Object dialogLock = new Object();
    private Parent root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        core.eventBus.register(this);
        this.root = new MainWindowView(core.eventBus, core.pipeline);
        /**
         * Any exceptions thrown by the UI will be caught here and an exception dialog will be displayed
         */
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            core.eventBus.post(new FatalErrorEvent(throwable));

        });

        root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

        stage.setTitle("GRIP Computer Vision Engine");
        stage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
        stage.setScene(new Scene(root));
        stage.show();

        core.loadOperations.run();
    }

    @Subscribe
    public final void onFatalErrorEvent(FatalErrorEvent error) {
        // Print throwable before showing the exception so that errors are in order in the console
        error.getThrowable().printStackTrace();
        Platform.runLater(() -> {
            synchronized (this.dialogLock) {
                try {
                    // Don't create more than one exception dialog at the same time
                    final ExceptionAlert exceptionAlert = new ExceptionAlert(root, error.getThrowable(), getHostServices());
                    exceptionAlert.showAndWait();
                } catch (RuntimeException e) {
                    // Well in this case something has gone very, very wrong
                    // We don't want to create a feedback loop either.
                    e.printStackTrace();
                    assert false : "Could not rethrow exception.";
                    Platform.exit();
                }
            }
        });
    }
}
