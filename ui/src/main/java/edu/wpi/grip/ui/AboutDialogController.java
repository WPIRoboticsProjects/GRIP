package edu.wpi.grip.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import javax.inject.Inject;

public class AboutDialogController {

    @Inject
    private Main main;

    @FXML
    private Label versionNumberLabel;

    @FXML
    void mousePressedDocumentationButton(MouseEvent event) {
        main.getHostServices().showDocument("http://wpilib.screenstepslive.com/s/4485/m/50711");
    }

    @FXML
    void mousePressedGithubButton(MouseEvent event) {
        main.getHostServices().showDocument("https://github.com/WPIRoboticsProjects/GRIP");
    }

    @FXML
    void initialize() {
        assert versionNumberLabel != null : "fx:id=\"versionNumberText\" was not injected: check your FXML file 'AboutDialog.fxml'.";

        versionNumberLabel.setText("Version " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());
    }
}
