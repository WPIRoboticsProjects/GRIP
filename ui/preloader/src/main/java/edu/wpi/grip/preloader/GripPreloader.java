package edu.wpi.grip.preloader;

import java.io.IOException;
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public final class GripPreloader extends Preloader {

  private static final Color primaryColor = Color.color(0.95, 1, 0.95);
  private static final Color secondaryColor = Color.color(0.5, 1, 0.8);

  // Animation timings
  private static final double minTime = 0.25;
  private static final double maxTime = 0.50;

  private static final double HEXAGON_RADIUS = 16;

  private Stage preloaderStage;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage preloaderStage) throws IOException {
    final StackPane root = FXMLLoader.load(GripPreloader.class.getResource("Preloader.fxml"));

    Screen screen = Screen.getPrimary();

    // Animated hexagon grid background

    Random random = new Random(System.currentTimeMillis() ^ (System.currentTimeMillis() >> 16));
    HexagonGrid hexagonGrid = new HexagonGrid(
        (int) ((screen.getBounds().getWidth() / 4) / HEXAGON_RADIUS),
        (int) ((screen.getBounds().getHeight() / 4) / HEXAGON_RADIUS),
        HEXAGON_RADIUS,
        2);
    // animate the hexagons
    hexagonGrid.hexagons()
        .stream()
        .map(h -> new FillTransition(
            Duration.seconds(
                clamp(random.nextGaussian() + 1, 0, 2) * (maxTime - minTime) + minTime),
            h, primaryColor, secondaryColor))
        .peek(t -> t.setCycleCount(Animation.INDEFINITE))
        .peek(t -> t.setAutoReverse(true))
        .forEach(t -> t.playFrom(Duration.seconds(random.nextDouble() * maxTime * 16)));
    Pane backgroundContainer = new Pane(hexagonGrid);

    // bring the hexagons to the top and left edges to avoid weird blank spots
    hexagonGrid.setTranslateX(-HEXAGON_RADIUS * 2);
    hexagonGrid.setTranslateY(-HEXAGON_RADIUS * 2);

    // make the background as small as possible (avoids whitespace on bottom and right edges)
    backgroundContainer.setPrefSize(0, 0);
    root.getChildren().add(0, backgroundContainer);

    Scene scene = new Scene(root);

    System.setProperty("prism.lcdtext", "false");

    if (getParameters().getRaw().contains("windowed")) {
      preloaderStage.initStyle(StageStyle.UNDECORATED);
    } else {
      preloaderStage.initStyle(StageStyle.TRANSPARENT);
    }
    preloaderStage.setScene(scene);
    preloaderStage.setAlwaysOnTop(true);
    preloaderStage.setResizable(false);
    preloaderStage.show();

    this.preloaderStage = preloaderStage;
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification pn) {
    if (pn instanceof StateChangeNotification
        && ((StateChangeNotification) pn).getType() == StateChangeNotification.Type.BEFORE_START) {
      preloaderStage.hide();
    }
  }

  private static double clamp(double n, double min, double max) {
    return (n < min) ? min : ((n > max) ? max : n);
  }

}
