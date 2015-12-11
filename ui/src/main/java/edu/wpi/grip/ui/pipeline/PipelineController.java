package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.*;
import edu.wpi.grip.ui.pipeline.input.InputSocketView;
import edu.wpi.grip.ui.pipeline.source.SourceView;
import edu.wpi.grip.ui.pipeline.source.SourceViewFactory;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JavaFX controller for the pipeline.  This controller renders a list of steps.
 */
public class PipelineController {

    @FXML private Parent root;
    @FXML private VBox sources;
    @FXML private Pane addSourcePane;
    @FXML private HBox stepBox;
    @FXML private Group connections;
    @Inject private EventBus eventBus;
    @Inject private Pipeline pipeline;
    @Inject private StepController.Factory stepControllerFactory;
    @Inject private Injector injector;

    private List<StepController> steps = new ArrayList<>();

    /**
     * Add initial views for the stuff in the pipeline at the time this controller is created
     */
    public void initialize() throws Exception {
        for (Source source : pipeline.getSources()) {
            sources.getChildren().add(SourceViewFactory.createSourceView(eventBus, source));
        }

        // Create a new controller and view for each initial step (see onStepAdded)
        for (Step step : pipeline.getSteps()) {
            final StepController stepController = stepControllerFactory.create(step);
            stepBox.getChildren().add(FXMLLoader.load(getClass().getResource("Step.fxml"), null, null, c -> stepController));
            steps.add(stepController);
        }

        addSourcePane.getChildren().add(new AddSourceView(eventBus));
    }

    /**
     * @return An unmodifiable list of {@link SourceView} corresponding to all of the sources in the pipeline
     */
    @SuppressWarnings("unchecked")
    public ObservableList<SourceView> getSources() {
        return (ObservableList) sources.getChildrenUnmodifiable();
    }

    /**
     * @return An unmodifiable list of the {@link ConnectionView}s corresponding to all of the connections in the
     * pipeline
     */
    @SuppressWarnings("unchecked")
    public ObservableList<ConnectionView> getConnections() {
        return (ObservableList) connections.getChildrenUnmodifiable().filtered(node -> node instanceof ConnectionView);
    }

    /**
     * @return The {@link InputSocketView} that corresponds with the given socket
     */
    private InputSocketView findInputSocketView(InputSocket socket) {
        for (StepController stepController : steps) {
            for (InputSocketView socketView : stepController.getInputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        throw new IllegalArgumentException("input socket view does not exist in pipeline: " + socket);
    }

    /**
     * @return The {@link OutputSocketView} that corresponds with the given socket, either from one of the steps in
     * the pipeline or from a source
     */
    private OutputSocketView findOutputSocketView(OutputSocket socket) {
        for (StepController stepController : steps) {
            for (OutputSocketView socketView : stepController.getOutputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        for (SourceView<?> sourceView : getSources()) {
            for (OutputSocketView socketView : sourceView.getOutputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        throw new IllegalArgumentException("output socket view does not exist in pipeline: " + socket);
    }

    /**
     * @return The {@link SourceView} that corresponds with the given source
     */
    public SourceView findSourceView(Source source) {
        for (SourceView sourceView : this.getSources()) {
            if (sourceView.getSource() == source) {
                return sourceView;
            }
        }

        throw new IllegalArgumentException("source view does not exist in pipeline: " + source);
    }

    /**
     * @return The {@link StepController} that corresponds with the given step
     */
    private StepController findStepController(Step step) {
        for (StepController stepController : steps) {
            if (stepController.getStep() == step) {
                return stepController;
            }
        }

        throw new IllegalArgumentException("step view does not exist in pipeline: " + step);
    }

    /**
     * @return The {@link ConnectionView} that corresponds with the given connection
     */
    private ConnectionView findConnectionView(Connection connection) {
        for (ConnectionView connectionView : getConnections()) {
            if (connectionView.getConnection() == connection) {
                return connectionView;
            }
        }

        throw new IllegalArgumentException("connection view does not exist in pipeline: " + connection);
    }

    /**
     * Add a view for the given connection to the pipeline view.  This method figures out the positioning and other
     * details of adding the connection.
     */
    private void addConnectionView(Connection connection) {
        PlatformImpl.runAndWait(() -> {
            // Before adding a connection control, we have to look up the controls for both sockets in the connection so
            // we know where to position it.
            final OutputSocketView outputSocketView = findOutputSocketView(connection.getOutputSocket());
            final InputSocketView inputSocketView = findInputSocketView(connection.getInputSocket());

            final ConnectionView connectionView = new ConnectionView(eventBus, connection);

            // Re-position the start and end points of the connection whenever one of the handles is moved.  This
            // happens, for example, when a step in the pipeline is deleted, so all of the steps after it shift left.
            final InvalidationListener handleListener = observable -> {
                synchronized (this) {
                    final Node outputHandle = outputSocketView.getHandle();
                    final Node inputHandle = inputSocketView.getHandle();

                    final Bounds outputSocketBounds =
                            root.sceneToLocal(outputHandle.localToScene(outputHandle.getLayoutBounds()));
                    final Bounds inputSocketBounds =
                            root.sceneToLocal(inputHandle.localToScene(inputHandle.getLayoutBounds()));

                    final double x1 = outputSocketBounds.getMinX() + outputSocketBounds.getWidth() / 2.0;
                    final double y1 = outputSocketBounds.getMinY() + outputSocketBounds.getHeight() / 2.0;
                    final double x2 = inputSocketBounds.getMinX() + inputSocketBounds.getWidth() / 2.0;
                    final double y2 = inputSocketBounds.getMinY() + inputSocketBounds.getHeight() / 2.0;

                    PlatformImpl.runAndWait(() -> {
                        connectionView.inputHandleProperty().setValue(new Point2D(x1, y1));
                        connectionView.outputHandleProperty().setValue(new Point2D(x2, y2));
                        ((ReadOnlyObjectProperty) observable).get();
                    });
                }
            };

            inputSocketView.localToSceneTransformProperty().addListener(handleListener);
            outputSocketView.localToSceneTransformProperty().addListener(handleListener);
            handleListener.invalidated(inputSocketView.localToSceneTransformProperty());

            connections.getChildren().add(connectionView);
        });
    }

    @Subscribe
    public void onSourceAdded(SourceAddedEvent event) {
        PlatformImpl.runAndWait(() ->
                sources.getChildren().add(
                        SourceViewFactory.createSourceView(eventBus, event.getSource())));
    }

    @Subscribe
    public void onSourceRemoved(SourceRemovedEvent event) {
        PlatformImpl.runAndWait(() -> sources.getChildren().remove(findSourceView(event.getSource())));
    }

    @Subscribe
    public void onStepAdded(StepAddedEvent event) {
        // Add a new view to the pipeline for the step that was added
        PlatformImpl.runAndWait(() -> {
            final int index = event.getIndex().or(stepBox.getChildren().size());
            final Step step = event.getStep();

            try {
                // Create a new controller for the step.  Doing this with our factory method instead of with JavaFX's
                // default controller factory lets us pass in a parameter - the step.
                final StepController stepController = stepControllerFactory.create(step);
                steps.add(stepController);
                stepBox.getChildren().add(index,
                        FXMLLoader.load(getClass().getResource("Step.fxml"), null, null, c -> stepController));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Subscribe
    public void onStepRemoved(StepRemovedEvent event) {
        // Remove the control that corresponds with the step that was removed
        PlatformImpl.runAndWait(() -> {
            final StepController stepController = findStepController(event.getStep());

            steps.remove(stepController);
            eventBus.unregister(stepController);

            stepBox.getChildren().remove(stepController.getRoot());
            eventBus.unregister(stepController.getRoot());
        });
    }

    @Subscribe
    public void onStepMoved(StepMovedEvent event) {
        PlatformImpl.runAndWait(() -> {
            final Node stepView = findStepController(event.getStep()).getRoot();
            final int oldIndex = stepBox.getChildren().indexOf(stepView);
            final int newIndex = Math.min(Math.max(oldIndex + event.getDistance(), 0), steps.size() - 1);
            if (newIndex != oldIndex) {
                stepBox.getChildren().remove(oldIndex);
                stepBox.getChildren().add(newIndex, stepView);
            }
        });
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        // Add the new connection view
        PlatformImpl.runAndWait(() -> addConnectionView(event.getConnection()));
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        // Remove the control that corresponds with the connection that was removed
        PlatformImpl.runAndWait(() -> {
            final ConnectionView connectionView = findConnectionView(event.getConnection());
            connections.getChildren().remove(connectionView);
            eventBus.unregister(connectionView);
        });
    }
}

