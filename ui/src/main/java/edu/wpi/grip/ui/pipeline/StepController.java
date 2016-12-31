package edu.wpi.grip.ui.pipeline;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.TimerEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.dragging.StepDragService;
import edu.wpi.grip.ui.events.SetStepsExpandedEvent;
import edu.wpi.grip.ui.pipeline.input.InputSocketController;
import edu.wpi.grip.ui.pipeline.input.InputSocketControllerFactory;
import edu.wpi.grip.ui.util.ControllerMap;
import edu.wpi.grip.ui.util.StyleClassNameUtility;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;

import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.inject.Inject;

/**
 * A JavaFX control that shows a step in the pipeline.  This control shows the name of the operation
 * as well as a list of input sockets and output sockets.
 */
@ParametrizedController(url = "Step.fxml")
public class StepController implements Controller {

  private final Pipeline pipeline;
  private final InputSocketControllerFactory inputSocketControllerFactory;
  private final OutputSocketController.Factory outputSocketControllerFactory;
  private final ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory;
  private final StepDragService stepDragService;
  private final EventBus eventBus;
  private final Step step;
  private final BooleanProperty expanded = new SimpleBooleanProperty(true);
  @FXML
  private VBox root;
  @FXML
  private Labeled title;
  @FXML
  private Button deleteButton;
  @FXML
  private Button moveLeftButton;
  @FXML
  private Button moveRightButton;
  @FXML
  private Label elapsedTime;
  @FXML
  private ImageView icon;
  @FXML
  private HBox buttons;
  @FXML
  private VBox inputs;
  @FXML
  private VBox outputs;
  @FXML
  private ImageView expandIcon;
  @FXML
  private Button expand;
  private ControllerMap<InputSocketController, Node> inputSocketMapManager;
  private ControllerMap<OutputSocketController, Node> outputSocketMapManager;

  private static final Image UP_ARROW = new Image("/edu/wpi/grip/ui/icons/up.png");
  private static final Image DOWN_ARROW = new Image("/edu/wpi/grip/ui/icons/down.png");
  private static final Predicate<InputSocketController> interactiveInputSocketFilter
      = i -> !i.getSocket().getSocketHint().getView().equals(SocketHint.View.NONE);

  @Inject
  StepController(Pipeline pipeline,
                 InputSocketControllerFactory inputSocketControllerFactory,
                 OutputSocketController.Factory outputSocketControllerFactory,
                 ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
                 StepDragService stepDragService,
                 EventBus eventBus,
                 @Assisted Step step) {
    this.pipeline = pipeline;
    this.inputSocketControllerFactory = inputSocketControllerFactory;
    this.outputSocketControllerFactory = outputSocketControllerFactory;
    this.exceptionWitnessResponderButtonFactory = exceptionWitnessResponderButtonFactory;
    this.stepDragService = stepDragService;
    this.eventBus = eventBus;
    this.step = step;
  }

  @FXML
  private void initialize() {
    inputSocketMapManager = new ControllerMap<>(inputs.getChildren());
    outputSocketMapManager = new ControllerMap<>(outputs.getChildren());

    root.getStyleClass().add(StyleClassNameUtility.classNameFor(step));
    title.setText(step.getOperationDescription().name());
    step.getOperationDescription().icon().ifPresent(icon -> this.icon.setImage(
        new Image(InputStream.class.cast(icon))));
    buttons.getChildren().add(0, exceptionWitnessResponderButtonFactory.create(step, "Step Error"));

    if (step.getInputSockets().stream()
        .allMatch(inputSocket -> inputSocket.getSocketHint().getView()
            .equals(SocketHint.View.NONE))) {
      expand.setManaged(false);
    } else {
      expandIcon.setImage(UP_ARROW);
      expanded.addListener(((observable, oldValue, newValue) -> {
        if (newValue) {
          inputSocketMapManager.keySet().stream()
              .filter(interactiveInputSocketFilter)
              .forEach(this::fadeIn);
          reopen();
          expandIcon.setImage(UP_ARROW);
        } else {
          inputSocketMapManager.keySet().stream()
              .filter(interactiveInputSocketFilter)
              .filter(i -> i.getSocket().getConnections().isEmpty())
              .forEach(this::fadeOut);
          closeUp();
          expandIcon.setImage(DOWN_ARROW);
        }
      }));
    }

    // Add a SocketControlView for each input socket and output socket
    for (InputSocket<?> inputSocket : step.getInputSockets()) {
      inputSocketMapManager.add(inputSocketControllerFactory.create(inputSocket));
    }

    for (OutputSocket<?> outputSocket : step.getOutputSockets()) {
      outputSocketMapManager.add(outputSocketControllerFactory.create(outputSocket));
    }

    root.setOnDragDetected(event -> {
      stepDragService.beginDrag(this.step, root, "step");
      event.consume();
    });

    root.setOnDragDone(event -> {
      stepDragService.completeDrag();
      event.consume();
    });

  }

  /**
   * An unmodifiable collection of {@link InputSocketController}s corresponding to the input sockets
   * of this step.
   */
  public Collection<InputSocketController> getInputSockets() {
    return inputSocketMapManager.keySet();
  }

  /**
   * An unmodifiable collection of {@link InputSocketController}s corresponding to the output
   * sockets of this step.
   */
  public Collection<OutputSocketController> getOutputSockets() {
    return outputSocketMapManager.keySet();
  }

  public VBox getRoot() {
    return root;
  }

  public Step getStep() {
    return step;
  }

  @FXML
  private void deleteStep() {
    pipeline.removeStep(step);
  }

  @FXML
  private void moveStepLeft() {
    pipeline.moveStep(step, -1);
  }

  @FXML
  private void moveStepRight() {
    pipeline.moveStep(step, +1);
  }

  @Subscribe
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void finished(TimerEvent event) {
    if (event.getTarget() != this.step) {
      return;
    }
    Platform.runLater(() ->
        elapsedTime.setText(String.format("Ran in %.1f ms", event.getElapsedTime() / 1e3)));
  }

  @Subscribe
  private void onBenchmark(BenchmarkEvent e) {
    Platform.runLater(() -> {
      deleteButton.setDisable(e.isStart());
      moveLeftButton.setDisable(e.isStart());
      moveRightButton.setDisable(e.isStart());
    });
  }

  /**
   * Clicking the arrow at the top of the step will cause the step to either expand or retract.
   * Secondary clicking the arrow at the top of the step will cause all steps to either expand or
   * retract.
   */
  @FXML
  private void toggleExpand(MouseEvent event) {
    if (event.getButton().equals(MouseButton.PRIMARY)) {
      expanded.set(!expanded.get());
    } else if (event.getButton().equals(MouseButton.SECONDARY)) {
      eventBus.post(new SetStepsExpandedEvent(!expanded.get()));
    }
  }

  @Subscribe
  public void setExpanded(SetStepsExpandedEvent event) {
    expanded.set(event.isExpanded());
  }

  /**
   * Makes an animation to make an input socket fade out over 0.1 seconds.
   *
   * @param input the input socket controller that will be faded out.
   */
  private void fadeOut(InputSocketController input) {
    DoubleProperty opacity = input.getRoot().opacityProperty();
    Timeline fadeOut = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
        new KeyFrame(new Duration(100), new KeyValue(opacity, 0.0)));
    fadeOut.setOnFinished(event -> {
      input.getRoot().setVisible(false);
      input.getRoot().setManaged(false);
    });
    fadeOut.play();
  }

  /**
   * Makes an animation to make an input socket fade in over 0.1 seconds.
   *
   * @param input the input socket controller that will be faded out.
   */
  private void fadeIn(InputSocketController input) {
    input.getRoot().setVisible(true);
    DoubleProperty opacity = input.getRoot().opacityProperty();
    Timeline fadeIn = new Timeline(
        new KeyFrame(new Duration(100), new KeyValue(opacity, 1.0)));
    fadeIn.setOnFinished(
        event -> inputSocketMapManager.keySet().forEach(i -> input.getRoot().setManaged(true)));
    fadeIn.play();
  }

  /**
   * Makes an animation to make the input vbox slide closed over .25 seconds
   */
  private void closeUp() {
    Timeline animation = new Timeline(
        new KeyFrame(Duration.seconds(0.25),
            new KeyValue(inputs.prefHeightProperty(), 0)));
    animation.play();
  }

  /**
   * Makes an animation to make the input vbox slide open over .1 seconds
   */
  private void reopen() {
    Timeline animation = new Timeline(
        new KeyFrame(Duration.seconds(0.1),
            new KeyValue(inputs.prefHeightProperty(), inputs.getMaxHeight())));
    animation.play();
  }

  /**
   * Used for assisted injects.  Guice will automatically create an instance of this interface so we
   * can create step controllers.  This lets us use injection with StepController even though it
   * requires a {@link Step} (which is not an injected dependency).
   */
  public interface Factory {
    StepController create(Step step);
  }
}
