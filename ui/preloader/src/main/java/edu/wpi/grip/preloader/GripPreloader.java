package edu.wpi.grip.preloader;

import java.io.IOException;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class GripPreloader extends Preloader {

  private ProgressBar progressBar;
  private Stage preloaderStage;

  @Override
  public void start(Stage preloaderStage) throws IOException {
    Parent root = FXMLLoader.load(GripPreloader.class.getResource("Preloader.fxml"));
    Scene scene = new Scene(root);

    progressBar = (ProgressBar) root.getChildrenUnmodifiable().filtered(
        p -> p instanceof ProgressBar).get(0);

    System.setProperty("prism.lcdtext", "false");
    preloaderStage.setScene(scene);
    preloaderStage.initStyle(StageStyle.TRANSPARENT);
    preloaderStage.setAlwaysOnTop(true);
    preloaderStage.setResizable(false);
    preloaderStage.show();

    this.preloaderStage = preloaderStage;
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification pn) {
    if (pn instanceof ProgressNotification) {
      progressBar.setProgress(((ProgressNotification) pn).getProgress());
    } else if (pn instanceof StateChangeNotification
        && ((StateChangeNotification) pn).getType() == StateChangeNotification.Type.BEFORE_START) {
      preloaderStage.hide();
    }
  }
}
