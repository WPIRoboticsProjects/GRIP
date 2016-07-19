package edu.wpi.grip.preloader;

import java.io.IOException;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class GripPreloader extends Preloader {

  private Stage preloaderStage;

  @Override
  public void start(Stage preloaderStage) throws IOException {
    Scene scene = new Scene(FXMLLoader.load(GripPreloader.class.getResource("Preloader.fxml")));

    preloaderStage.setScene(scene);
    preloaderStage.initStyle(StageStyle.TRANSPARENT);
    preloaderStage.setAlwaysOnTop(true);
    preloaderStage.setResizable(false);
    preloaderStage.show();

    this.preloaderStage = preloaderStage;
  }

  @Override
  public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
    if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
      preloaderStage.hide();
    }
  }
}
