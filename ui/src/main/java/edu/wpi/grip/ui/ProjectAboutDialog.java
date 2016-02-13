package edu.wpi.grip.ui;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

/**
 * A JavaFX dialog that displays information about the application (such as the version number)
 */
public class ProjectAboutDialog extends Dialog<ButtonType> {

    public ProjectAboutDialog(Parent root) {
        super();

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_LEFT);

        ImageView img = new ImageView(new Image("edu/wpi/grip/ui/icons/grip-title.png"));

        grid.add(img, 0, 0);

        Label versionLabel = new Label("Version " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());
        // makes the version label look a little bit better aligned (looks too far to the left otherwise)
        versionLabel.setPadding(new Insets(0, 0, 0, 1));
        Font versionLabelFont = new Font(16.0);
        versionLabel.setFont(versionLabelFont);
        grid.add(versionLabel, 0, 1, 3, 1);

        Hyperlink githubLink = new Hyperlink();
        githubLink.setText("GRIP on Github");
        githubLink.setPadding(new Insets(0, 0, 0, 0));
        githubLink.setOnAction(event -> {
            HostServicesFactory.getInstance(Main.getInstance()).showDocument("https://github.com/WPIRoboticsProjects/GRIP");
        });
        grid.add(githubLink, 0, 2);

        Hyperlink issuesLink = new Hyperlink();
        issuesLink.setText("Issues List");
        issuesLink.setPadding(new Insets(0, 0, 0, 0));
        issuesLink.setOnAction(event -> {
            HostServicesFactory.getInstance(Main.getInstance()).showDocument("https://github.com/WPIRoboticsProjects/GRIP/issues");
        });
        grid.add(issuesLink, 0, 3);

        Hyperlink wikiLink = new Hyperlink();
        wikiLink.setText("Wiki");
        wikiLink.setPadding(new Insets(0, 0, 0, 0));
        wikiLink.setOnAction(event -> {
            HostServicesFactory.getInstance(Main.getInstance()).showDocument("https://github.com/WPIRoboticsProjects/GRIP/wiki");
        });
        grid.add(wikiLink, 0, 4);

        Hyperlink operationsLink = new Hyperlink();
        operationsLink.setText("Operation Reference");
        operationsLink.setPadding(new Insets(0, 0, 0, 0));
        operationsLink.setOnAction(event -> {
            HostServicesFactory.getInstance(Main.getInstance()).showDocument("https://github.com/WPIRoboticsProjects/GRIP/wiki/Operation-Reference-Table");
        });
        grid.add(operationsLink, 0, 5);

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
