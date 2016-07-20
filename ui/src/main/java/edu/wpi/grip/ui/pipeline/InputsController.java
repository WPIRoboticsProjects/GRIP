package edu.wpi.grip.ui.pipeline;

import com.google.inject.assistedinject.Assisted;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.dragging.StepDragService;
import edu.wpi.grip.ui.pipeline.input.InputSocketController;
import edu.wpi.grip.ui.pipeline.input.InputSocketControllerFactory;
import edu.wpi.grip.ui.util.ControllerMap;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A JavaFX control that shows inputs in a step.
 */
@ParametrizedController(url = "Inputs.fxml")
public class InputsController implements Controller {

  private final InputSocketControllerFactory inputSocketControllerFactory;
  private final List<InputSocket> inputs;

  @FXML
  private VBox root;
  private ControllerMap<InputSocketController, Node> inputSocketMapManager;

  @Inject
  InputsController(InputSocketControllerFactory inputSocketControllerFactory,
                   @Assisted List<InputSocket> inputs) {

    this.inputSocketControllerFactory = inputSocketControllerFactory;
    this.inputs = inputs;
  }

  @FXML
  private void initialize() {
    inputSocketMapManager = new ControllerMap<>(root.getChildren());

    // Add a SocketControlView for each input socket and output socket
    for (InputSocket<?> inputSocket : inputs) {
      inputSocketMapManager.add(inputSocketControllerFactory.create(inputSocket));
    }

  }

  /**
   * An unmodifiable collection of {@link InputSocketController}s corresponding to the input sockets
   * of this step.
   */
  public Collection<InputSocketController> getInputSockets() {
    return inputSocketMapManager.keySet();
  }


  public VBox getRoot() {
    return root;
  }


  /**
   * Used for assisted injects.  Guice will automatically create an instance of this interface so we
   * can create Inputs controllers.  This lets us use injection with InputsController
   */
  public interface Factory {
    InputsController create(List<InputSocket> inputs);
  }
}
