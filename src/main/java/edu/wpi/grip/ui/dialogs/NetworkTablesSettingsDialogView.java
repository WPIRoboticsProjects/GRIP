package edu.wpi.grip.ui.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.DialogPane;

import java.io.IOException;


public class NetworkTablesSettingsDialogView extends DialogPane {

    public NetworkTablesSettingsDialogView(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NetworkTablesSettings.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
