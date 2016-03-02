package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.*;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.pipeline.input.InputSocketController;
import edu.wpi.grip.ui.pipeline.source.SourceController;
import edu.wpi.grip.ui.pipeline.source.SourceControllerFactory;
import edu.wpi.grip.ui.util.ControllerMap;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.Collection;

/**
 * A JavaFX controller for the pipeline.  This controller renders a list of steps.
 */
@Singleton
@ParametrizedController(url = "Pipeline.fxml")
public final class PipelineController {

    @FXML
    private Parent root;
    @FXML
    private VBox sourcesBox;
    @FXML
    private Pane addSourcePane;
    @FXML
    private HBox stepBox;
    @FXML
    private Group connections;

    @Inject
    private EventBus eventBus;
    @Inject
    private Pipeline pipeline;
    @Inject
    private SourceControllerFactory sourceControllerFactory;
    @Inject
    private StepController.Factory stepControllerFactory;
    @Inject
    private AddSourceView addSourceView;

    private ControllerMap<StepController, Node> stepsMapManager;
    private ControllerMap<SourceController, Node> sourceMapManager;

    /**
     * Add initial views for the stuff in the pipeline at the time this controller is created
     */
    @FXML
    public void initialize() throws Exception {
        stepsMapManager = new ControllerMap<>(stepBox.getChildren());
        sourceMapManager = new ControllerMap<>(sourcesBox.getChildren());

        pipeline.getSources().forEach(source -> {
            final SourceController sourceController = sourceControllerFactory.create(source);
            sourceMapManager.add(sourceController);
        });

        // Create a new controller and view for each initial step (see onStepAdded)
        pipeline.getSteps().forEach(step -> {
            final StepController stepController = stepControllerFactory.create(step);
            stepsMapManager.add(stepController);
        });

        addSourcePane.getChildren().add(addSourceView);
    }

    /**
     * @return An unmodifiable list of {@link SourceController} corresponding to all of the sources in the pipeline
     */
    public Collection<SourceController> getSources() {
        return sourceMapManager.keySet();
    }

    /**
     * @return An unmodifiable list of the {@link ConnectionView}s corresponding to all of the connections in the
     * pipeline
     */
    @SuppressWarnings("unchecked")
    public Collection<ConnectionView> getConnections() {
        return (ObservableList) connections.getChildrenUnmodifiable().filtered(node -> node instanceof ConnectionView);
    }

    /**
     * @return The {@link InputSocketController} that corresponds with the given socket
     */
    private InputSocketController findInputSocketView(InputSocket socket) {
        for (StepController stepController : stepsMapManager.keySet()) {
            for (InputSocketController socketView : stepController.getInputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }
        throw new IllegalArgumentException("input socket view does not exist in pipeline: " + socket);
    }

    /**
     * @return The {@link OutputSocketController} that corresponds with the given socket, either from one of the steps in
     * the pipeline or from a source
     */
    private OutputSocketController findOutputSocketView(OutputSocket socket) {
        for (StepController stepController : stepsMapManager.keySet()) {
            for (OutputSocketController socketView : stepController.getOutputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        for (SourceController<?> sourceView : getSources()) {
            for (OutputSocketController socketView : sourceView.getOutputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        throw new IllegalArgumentException("output socket view does not exist in pipeline: " + socket);
    }

    /**
     * @return The {@link SourceController} that corresponds with the given source
     */
    public SourceController findSourceView(Source source) {
        for (SourceController sourceView : getSources()) {
            if (sourceView.getSource() == source) {
                return sourceView;
            }
        }

        throw new IllegalArgumentException("source view does not exist in pipeline: " + source);
    }

    /**
     * @return The {@link StepController} that corresponds with the given step
     */
    public StepController findStepController(Step step) {
        for (StepController stepController : stepsMapManager.keySet()) {
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
        // runAndWait() is used here because we don't want the main thread to resume, possibly causing more actions
        // that manipulate the list of connections, until the GUI component is added.  This prevents rendering issues.
        // Since the code in here JUST manipulates JavaFX components and doesn't post stuff to the EventBus, it's safe
        // to put in runAndWait() without fear of deadlock.
        PlatformImpl.runAndWait(() -> {
            // Before adding a connection control, we have to look up the controls for both sockets in the connection so
            // we know where to position it.
            final OutputSocketController outputSocketView = findOutputSocketView(connection.getOutputSocket());
            final InputSocketController inputSocketController = findInputSocketView(connection.getInputSocket());

            final ConnectionView connectionView = new ConnectionView(connection);

            // Re-position the start and end points of the connection whenever one of the handles is moved.  This
            // happens, for example, when a step in the pipeline is deleted, so all of the steps after it shift left.
            final InvalidationListener handleListener = observable -> {
                synchronized (this) {
                    final Node outputHandle = outputSocketView.getHandle();
                    final Node inputHandle = inputSocketController.getHandle();

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

            inputSocketController.getHandle().localToSceneTransformProperty().addListener(handleListener);
            outputSocketView.getHandle().localToSceneTransformProperty().addListener(handleListener);
            handleListener.invalidated(outputSocketView.getHandle().localToSceneTransformProperty());

            connections.getChildren().add(connectionView);
        });
    }

    @Subscribe
    public void onSourceAdded(SourceAddedEvent event) {
        PlatformImpl.runAndWait(() -> {
            final SourceController sourceController = sourceControllerFactory.create(event.getSource());
            sourceMapManager.add(sourceController);
        });
    }

    @Subscribe
    public void onSourceRemoved(SourceRemovedEvent event) {
        PlatformImpl.runAndWait(() -> {
            final SourceController sourceController = findSourceView(event.getSource());
            sourceMapManager.remove(sourceController);
            eventBus.unregister(sourceController);
        });
    }

    @Subscribe
    public void onStepAdded(StepAddedEvent event) {
        // Add a new view to the pipeline for the step that was added
        PlatformImpl.runAndWait(() -> {
            final int index = event.getIndex().orElse(stepBox.getChildren().size());
            final Step step = event.getStep();

            // Create a new controller for the step.  Doing this with our factory method instead of with JavaFX's
            // default controller factory lets us pass in a parameter - the step.
            final StepController stepController = stepControllerFactory.create(step);
            stepsMapManager.add(index, stepController);
        });
    }

    @Subscribe
    public void onStepRemoved(StepRemovedEvent event) {
        // Remove the control that corresponds with the step that was removed
        PlatformImpl.runAndWait(() -> {
            final StepController stepController = findStepController(event.getStep());

            stepsMapManager.remove(stepController);
            eventBus.unregister(stepController);
        });
    }

    @Subscribe
    public void onStepMoved(StepMovedEvent event) {
        PlatformImpl.runAndWait(() -> {
            final StepController stepController = findStepController(event.getStep());
            stepsMapManager.moveByDistance(stepController, event.getDistance());
        });
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        // Add the new connection view
        addConnectionView(event.getConnection());
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

