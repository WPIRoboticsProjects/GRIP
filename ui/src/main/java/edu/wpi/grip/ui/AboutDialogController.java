package edu.wpi.grip.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class AboutDialogController {

    @Inject
    private Main main;

    @FXML
    private Text versionNumberText;

    @FXML
    void githubButtonAction(ActionEvent event) {
        main.getHostServices().showDocument("https://github.com/WPIRoboticsProjects/GRIP");
    }

    @FXML
    void screenstepsButtonAction(ActionEvent event) {
        main.getHostServices().showDocument("http://wpilib.screenstepslive.com/s/4485/m/50711");
    }

    @FXML
    void initialize() {
        assert versionNumberText != null : "fx:id=\"versionNumberText\" was not injected: check your FXML file 'AboutDialog.fxml'.";

        versionNumberText.setText("Version " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());
    }
}

