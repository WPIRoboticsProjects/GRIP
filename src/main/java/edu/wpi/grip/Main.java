package edu.wpi.grip;

import edu.wpi.grip.ui.ExceptionAlert;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ui/MainWindow.fxml"));
        final Parent root = loader.load();

        /**
         * Any exceptions thrown by the UI will be caught here and an exception dialog will be displayed
         */
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            // Print throwable before showing the exception so that errors are in order in the console.
            throwable.printStackTrace();
            Platform.runLater(() -> {
                try {
                    final ExceptionAlert exceptionAlert = new ExceptionAlert(root, throwable, getHostServices());
                    exceptionAlert.showAndWait();
                } catch (RuntimeException e) {
                    // Well in this case something has gone very, very wrong
                    // We don't want to create a feedback loop either.
                    e.printStackTrace();
                    assert false : "Could not rethrow exception.";
                }
            });
        });

        // Set the root font size based on the DPI of the primary screen.  As long as all sizes are defined in ems,
        // this means the GUI will be the same physical size on high DPI displays as it is on normal displays.
        final double fontSizePoints = 9.0;
        final double fontSizePixels = Screen.getPrimary().getDpi() * fontSizePoints / 72.0;
        root.setStyle("-fx-font-size: " + fontSizePixels + "px");

        stage.setTitle("GRIP Computer Vision Engine");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
