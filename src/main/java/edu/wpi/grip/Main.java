package edu.wpi.grip;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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
        Parent root = FXMLLoader.load(getClass().getResource("/edu/wpi/grip/ui/MainWindow.fxml"));

        // Set the root font size based on the DPI of the primary screen.  As long as all sizes are defined in ems,
        // this means the GUI will be the same physical size on high DPI displays as it is on normal displays.
        final double fontSizePoints = 9.0;
        final double fontSizePixels = Screen.getPrimary().getDpi() * fontSizePoints / 72.0;
        root.setStyle("-fx-font-size: " + fontSizePixels/2 + "px");

        Scene scene = new Scene(root);

        stage.setTitle("GRIP Computer Vision Engine");
        stage.setScene(scene);
        stage.show();
    }
}
