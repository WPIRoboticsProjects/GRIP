package edu.wpi.grip.ui;

import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A JavaFX dialog that displays information about the application (such as the version number)
 */
public class ProjectAboutDialog extends Dialog<ButtonType> {

    public ProjectAboutDialog(Parent root, ProjectSettings projectSettings) {
        super();

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_LEFT);

        Label versionLabel = new Label("GRIP Version: " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());
        // fixes the weird offset with the hyperlinks being 1 pixel too far to the right
        versionLabel.setPadding(new Insets(0, 0, 0, 1));
        grid.add(versionLabel, 0, 0, 3, 1);

        Hyperlink githubLink = new Hyperlink();
        githubLink.setText("GRIP on Github");
        githubLink.setPadding(new Insets(0, 0, 0, 0));
        githubLink.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/WPIRoboticsProjects/GRIP"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(githubLink, 0, 1);

        Hyperlink issuesLink = new Hyperlink();
        issuesLink.setText("Issues List");
        issuesLink.setPadding(new Insets(0, 0, 0, 0));
        issuesLink.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/WPIRoboticsProjects/GRIP/issues"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(issuesLink, 0, 2);

        Hyperlink wikiLink = new Hyperlink();
        wikiLink.setText("Wiki");
        wikiLink.setPadding(new Insets(0, 0, 0, 0));
        wikiLink.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/WPIRoboticsProjects/GRIP/wiki"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(wikiLink, 0, 3);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().setAll(ButtonType.OK);
        pane.setContent(grid);
        pane.styleProperty().bind(root.styleProperty());
        pane.getStylesheets().addAll(root.getStylesheets());
        pane.setPrefSize(DPIUtility.ABOUT_DIALOG_WIDTH, DPIUtility.ABOUT_DIALOG_HEIGHT );

        setTitle("About GRIP");
        setHeaderText("About GRIP");
        setResizable(true);
    }
}
