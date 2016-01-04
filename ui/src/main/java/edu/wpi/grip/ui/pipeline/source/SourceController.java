package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;
import edu.wpi.grip.ui.pipeline.StepController;
import edu.wpi.grip.ui.util.ControllerMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collection;

/**
 * A JavaFX control that represents a {@link Source}.  <code>SourceController</code>s are somewhat analogous to
 * {@link StepController}s in thatthe pipeline contrains them and they contain some sockets, but <code>SourceController</code>s
 * only have output sockets, and they show up in a different place.
 *
 * @param <S> The type of Source this view is for.
 */
@ParametrizedController(url = "Source.fxml")
public class SourceController<S extends Source> implements Controller {

    @FXML
    private VBox root;

    @FXML
    private Label name;

    @FXML
    private VBox sockets;

    @FXML
    private HBox controls;

    private final EventBus eventBus;
    private final OutputSocketController.Factory outputSocketControllerFactory;
    private final ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory;
    private final S source;
    private ControllerMap<OutputSocketController, Node> outputSocketMapManager;

    public interface BaseSourceControllerFactory<S extends Source> {
        SourceController<S> create(S source);
    }

    @Inject
    SourceController(
            final EventBus eventBus,
            final OutputSocketController.Factory outputSocketControllerFactory,
            final ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
            @Assisted final S source) {
        this.eventBus = eventBus;
        this.outputSocketControllerFactory = outputSocketControllerFactory;
        this.exceptionWitnessResponderButtonFactory = exceptionWitnessResponderButtonFactory;
        this.source = source;
    }

    @FXML
    public void initialize() throws Exception {
        outputSocketMapManager = new ControllerMap<>(sockets.getChildren());
        this.name.setText(source.getName());

        addControls(exceptionWitnessResponderButtonFactory.create(source, source.getClass().getSimpleName() + " Error"));

        for (OutputSocket<?> socket : source.getOutputSockets()) {
            outputSocketMapManager.add(outputSocketControllerFactory.create(socket));
        }
    }

    public S getSource() {
        return this.source;
    }

    /**
     * Adds the given Nodes to the node that should hold the controls.
     */
    protected void addControls(Node... control) {
        controls.getChildren().addAll(control);
    }

    /**
     * @return An unmodifiable list of {@link OutputSocketController}s corresponding to the sockets that this source produces
     */
    public Collection<OutputSocketController> getOutputSockets() {
        return this.outputSocketMapManager.keySet();
    }

    public VBox getRoot() {
        return root;
    }

    @FXML
    public void delete() {
        this.eventBus.post(new SourceRemovedEvent(this.getSource()));
    }
}
