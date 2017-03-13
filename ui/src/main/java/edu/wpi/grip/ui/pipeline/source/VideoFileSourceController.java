package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.sources.VideoFileSource;
import edu.wpi.grip.core.util.Pausable;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;

public class VideoFileSourceController extends SourceController<VideoFileSource> {

  @Inject
  VideoFileSourceController(
      EventBus eventBus,
      OutputSocketController.Factory outputSocketControllerFactory,
      ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
      @Assisted VideoFileSource source) {
    super(eventBus, outputSocketControllerFactory, exceptionWitnessResponderButtonFactory, source);
  }

  @Override
  protected void initialize() throws Exception {
    super.initialize();
    if (getSource().getFrameCount() > 1) {
      // Only add controls if there's multiple frames
      // (it's possible to load static image files, for example)
      addControls(
          new PlayPauseButton(getSource()),
          new Scrubber(getSource())
      );
    }
  }

  public interface Factory {
    VideoFileSourceController create(VideoFileSource source);
  }

  /**
   * A slider to allow users to control which frames are output.
   */
  private static class Scrubber extends Slider {

    private volatile boolean viewMode = true;

    public Scrubber(VideoFileSource source) {
      setPrefWidth(50);
      setMin(0);
      setMax(source.getFrameCount());
      setValue(source.getFramePosition());
      source.currentFrameProperty().addObserver((prev, cur) -> {
        if (viewMode) {
          Platform.runLater(() -> setValue(cur));
        }
      });
      valueProperty().addListener((obs, oldPos, newPos) -> {
        if (!viewMode && oldPos.intValue() != newPos.intValue()) {
          source.setFrame(newPos.intValue());
        }
      });
      setOnKeyPressed(e -> viewMode = false);
      setOnKeyReleased(e -> viewMode = true);
      setOnMousePressed(e -> viewMode = false);
      setOnMouseReleased(e -> viewMode = true);
    }

  }

  private static class PlayPauseButton extends ToggleButton {

    private final ImageView pause = new ImageView("/edu/wpi/grip/ui/icons/pause.png");
    private final ImageView resume = new ImageView("/edu/wpi/grip/ui/icons/start.png");

    public PlayPauseButton(Pausable pausable) {
      pause.setPreserveRatio(true);
      resume.setPreserveRatio(true);
      pause.setFitHeight(10);
      resume.setFitHeight(10);
      setGraphic(pause);
      pausable.pausedProperty().addObserver((prev, cur) -> {
        Platform.runLater(() -> setSelected(cur));
      });
      selectedProperty().addListener((obs, o, n) -> pausable.setPaused(n));
      selectedProperty().addListener((obs, o, n) -> {
        if (n) {
          setGraphic(resume);
        } else {
          setGraphic(pause);
        }
      });
    }

  }

}
