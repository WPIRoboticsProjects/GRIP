package edu.wpi.grip.ui;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import javax.inject.Inject;

public class AboutDialogController {

    @Inject
    private Main main;

    @FXML
    private Text versionNumberText;

    @FXML
    private Rectangle githubRectangle;

    @FXML
    private Text githubText;

    @FXML
    private Rectangle documentationRectangle;

    @FXML
    private Text documentationText;

    private static final Color CLEAR_COLOR = new Color(0.0, 0.0, 0.0, 0.0);

    @FXML
    void mouseEnteredDocumentationButton(MouseEvent event) {
        documentationRectangle.setFill(Color.GRAY);
        documentationText.setFill(Color.WHITE);
    }

    @FXML
    void mouseEnteredGithubButton(MouseEvent event) {
        githubRectangle.setFill(Color.GRAY);
        githubText.setFill(Color.WHITE);
    }

    @FXML
    void mouseExitedDocumentationButton(MouseEvent event) {
        documentationRectangle.setFill(CLEAR_COLOR);
        documentationText.setFill(Color.BLACK);
    }

    @FXML
    void mouseExitedGithubButton(MouseEvent event) {
        githubRectangle.setFill(CLEAR_COLOR);
        githubText.setFill(Color.BLACK);
    }

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
        assert versionNumberText != null : "fx:id=\"versionNumberText\" was not injected: check your FXML file 'AboutDialog.fxml'.";
        assert githubRectangle != null : "fx:id=\"githubRectangle\" was not injected: check your FXML file 'AboutDialog.fxml'.";
        assert githubText != null : "fx:id=\"githubText\" was not injected: check your FXML file 'AboutDialog.fxml'.";
        assert documentationRectangle != null : "fx:id=\"documentationRectangle\" was not injected: check your FXML file 'AboutDialog.fxml'.";
        assert documentationText != null : "fx:id=\"documentationText\" was not injected: check your FXML file 'AboutDialog.fxml'.";

        versionNumberText.setText("Version " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());
    }
}

