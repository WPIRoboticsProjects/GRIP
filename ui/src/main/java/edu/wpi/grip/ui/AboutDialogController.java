package edu.wpi.grip.ui;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class AboutDialogController {

    @FXML
    private Text versionNumberText;

    @FXML
    void initialize() {
        assert versionNumberText != null : "fx:id=\"versionNumberText\" was not injected: check your FXML file 'AboutDialog.fxml'.";

        versionNumberText.setText("Version " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());
    }
}

