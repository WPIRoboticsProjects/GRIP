package edu.wpi.grip.ui.pipeline;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.dragging.StepDragService;
import edu.wpi.grip.ui.pipeline.input.InputSocketController;
import edu.wpi.grip.ui.util.ControllerMap;
import edu.wpi.grip.ui.util.StyleClassNameUtility;

import com.google.inject.assistedinject.Assisted;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

  private static final int splitSize = 6;

  private final Pipeline pipeline;
  private final InputsController.Factory inputsControllerFactory;
  private final OutputSocketController.Factory outputSocketControllerFactory;
  private final ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory;
  private final StepDragService stepDragService;
  private final Step step;
  @FXML
  private VBox root;
  @FXML
  private Labeled title;
  @FXML
  private ImageView icon;
  @FXML
  private ImageView expandIcon;
  @FXML
  private HBox buttons;
  @FXML
  private HBox inputs;
  @FXML
  private VBox outputs;
  @FXML
  private Button expand;
  private boolean expanded = true;
  private ControllerMap<InputsController, Node> inputsMapManager;
  private ControllerMap<OutputSocketController, Node> outputSocketMapManager;

  @Inject
  StepController(Pipeline pipeline,
                 InputsController.Factory inputsControllerFactory,
                 OutputSocketController.Factory outputSocketControllerFactory,
                 ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
                 StepDragService stepDragService,
                 @Assisted Step step) {
    this.pipeline = pipeline;
    this.inputsControllerFactory = inputsControllerFactory;
    this.outputSocketControllerFactory = outputSocketControllerFactory;
    this.exceptionWitnessResponderButtonFactory = exceptionWitnessResponderButtonFactory;
    this.stepDragService = stepDragService;
    this.step = step;
  }

  @FXML
  private void initialize() {
    inputsMapManager = new ControllerMap<>(inputs.getChildren());
    outputSocketMapManager = new ControllerMap<>(outputs.getChildren());

    root.getStyleClass().add(StyleClassNameUtility.classNameFor(step));
    title.setText(step.getOperationDescription().name());
    step.getOperationDescription().icon().ifPresent(icon -> this.icon.setImage(
        new Image(InputStream.class.cast(icon))));
    buttons.getChildren().add(0, exceptionWitnessResponderButtonFactory.create(step, "Step Error"));

    if (step.getInputSockets().size() > splitSize) {
      expandIcon.setImage(new Image("/edu/wpi/grip/ui/icons/left-expand.png"));
    } else {
      expandIcon.fitWidthProperty().setValue(0);
    }
    setUpSockets();

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
  public Collection<InputsController> getInputs() {
    return inputsMapManager.keySet();
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

  @FXML
  private void expand() {
    if (step.getInputSockets().size() > splitSize) {

      inputsMapManager.clear();
      if (expanded) {
        expanded = false;
        expandIcon.setImage(new Image("/edu/wpi/grip/ui/icons/right-expand.png"));
      } else {
        expanded = true;
        expandIcon.setImage(new Image("/edu/wpi/grip/ui/icons/left-expand.png"));
      }
      setUpSockets();
    }
  }

  private void setUpSockets() {
    final int numSplits = step.getInputSockets().size() / splitSize + 1;
    int extra = step.getInputSockets().size() % numSplits;
    int index = 0;
    for (int i = 0; i < numSplits; i++) {
      List<InputSocket> tmpInputs = new ArrayList();
      for (int j = 0; j < step.getInputSockets().size() / numSplits; j++) {
        tmpInputs.add(step.getInputSockets().get(index));
        index++;
      }
      if (extra > 0) {
        tmpInputs.add(step.getInputSockets().get(index));
        index++;
        extra--;
      }
      inputsMapManager.add(inputsControllerFactory.create(tmpInputs));
      if (!expanded) {
        break;
      }
      if (i > 0) {
        //Fade in
        DoubleProperty opacity = inputs.getChildren().get(i).opacityProperty();
        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
            new KeyFrame(new Duration(500), new KeyValue(opacity, 1.0))
        );
        fadeIn.play();
      }
    }

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
