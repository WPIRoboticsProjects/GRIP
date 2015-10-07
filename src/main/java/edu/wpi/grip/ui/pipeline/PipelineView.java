package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control fro the pipeline.  This control renders a list of steps.
 */
public class PipelineView extends StackPane implements Initializable {
    @FXML
    private HBox steps;

    @FXML
    private Group connections;

    private final EventBus eventBus;
    private final Pipeline pipeline;

    public PipelineView(EventBus eventBus, Pipeline pipeline) {
        checkNotNull(eventBus);
        checkNotNull(pipeline);

        this.eventBus = eventBus;
        this.pipeline = pipeline;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Pipeline.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.eventBus.register(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (Step step : pipeline.getSteps()) {
            steps.getChildren().add(new StepView(this.eventBus, step));
        }

        connections.getChildren().add(new Rectangle(0, 0, 1, 1));
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    /**
     * @return An unmodifiable list of {@link StepView}s corresponding to all of the steps in the pipeline
     */
    @SuppressWarnings("unchecked")
    public ObservableList<StepView> getSteps() {
        return (ObservableList) this.steps.getChildrenUnmodifiable();
    }

    /**
     * @return An unmodifiable list of the {@link ConnectionView}s corresponding to all of the connections in the
     * pipeline
     */
    @SuppressWarnings("unchecked")
    public ObservableList<ConnectionView> getConnections() {
        return (ObservableList) this.connections.getChildrenUnmodifiable().filtered(node -> node instanceof ConnectionView);
    }

    /**
     * @return The {@link InputSocketView} that corresponds with the given socket
     */
    private InputSocketView findInputSocketView(Socket socket) {
        for (StepView stepView : this.getSteps()) {
            for (InputSocketView socketView : stepView.getInputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        return null;
    }

    /**
     * @return The {@link OutputSocketView} that corresponds with the given socket
     */
    private OutputSocketView findOutputSocketView(Socket socket) {
        for (StepView stepView : this.getSteps()) {
            for (OutputSocketView socketView : stepView.getOutputSockets()) {
                if (socketView.getSocket() == socket) {
                    return socketView;
                }
            }
        }

        return null;
    }

    /**
     * @return The {@link StepView} that corresponds with the given step
     */
    private StepView findStepView(Step step) {
        for (StepView stepView : this.getSteps()) {
            if (stepView.getStep() == step) {
                return stepView;
            }
        }

        return null;
    }

    /**
     * @return The {@link ConnectionView} that corresponds with the given connection
     */
    private ConnectionView findConnectionView(Connection connection) {
        for (ConnectionView connectionView : this.getConnections()) {
            if (connectionView.getConnection() == connection) {
                return connectionView;
            }
        }

        return null;
    }

    /**
     * Add a view for the given connection to the pipeline view.  This method figures out the positioning and other
     * details of adding the connection.
     */
    private void addConnectionView(Connection connection) {
        Platform.runLater(() -> {
            // Before adding a connection control, we have to look up the controls for both sockets in the connection so
            // we know where to position it.
            final OutputSocketView outputSocketView = findOutputSocketView(connection.getOutputSocket());
            final InputSocketView inputSocketView = findInputSocketView(connection.getInputSocket());

            if (inputSocketView == null || outputSocketView == null) {
                throw new RuntimeException("Connection added for socket that does not exist in the pipeline");
            }

            final ConnectionView connectionView = new ConnectionView(this.eventBus, connection);

            // Re-position the start and end points of the connection whenever one of the handles is moved.  This
            // happens, for example, when a step in the pipeline is deleted, so all of the steps after it shift left.
            final InvalidationListener handleListener = observable -> {
                synchronized (this) {
                    final Node outputHandle = outputSocketView.getHandle();
                    final Node inputHandle = inputSocketView.getHandle();
                    final Bounds outputSocketBounds = this.sceneToLocal(outputHandle.localToScene(outputHandle.getLayoutBounds()));
                    final Bounds inputSocketBounds = this.sceneToLocal(inputHandle.localToScene(inputHandle.getLayoutBounds()));
                    final double x1 = outputSocketBounds.getMinX() + outputSocketBounds.getWidth() / 2.0;
                    final double y1 = outputSocketBounds.getMinY() + outputSocketBounds.getHeight() / 2.0;
                    final double x2 = inputSocketBounds.getMinX() + inputSocketBounds.getWidth() / 2.0;
                    final double y2 = inputSocketBounds.getMinY() + inputSocketBounds.getHeight() / 2.0;

                    Platform.runLater(() -> {
                        connectionView.inputHandleProperty().setValue(new Point2D(x1, y1));
                        connectionView.outputHandleProperty().setValue(new Point2D(x2, y2));
                        ((ReadOnlyObjectProperty) observable).get();
                    });
                }
            };

            inputSocketView.localToSceneTransformProperty().addListener(handleListener);
            outputSocketView.localToSceneTransformProperty().addListener(handleListener);
            handleListener.invalidated(inputSocketView.localToSceneTransformProperty());

            this.connections.getChildren().add(connectionView);
        });
    }

    @Subscribe
    public void onStepAdded(StepAddedEvent event) {
        // Add a new control to the pipelineview for the step that was added
        Platform.runLater(() -> {
            synchronized (this) {
                int index = event.getIndex().or(this.steps.getChildren().size());
                this.steps.getChildren().add(index, new StepView(this.eventBus, event.getStep()));
            }
        });
    }

    @Subscribe
    public void onStepRemoved(StepRemovedEvent event) {
        // Remove the control that corresponds with the step that was removed
        Platform.runLater(() -> {
            synchronized (this) {
                final StepView stepView = findStepView(event.getStep());
                if (stepView != null) {
                    this.steps.getChildren().remove(stepView);
                    this.eventBus.unregister(stepView);
                }
            }
        });
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        final Connection connection = event.getConnection();

        // Add the new connection view
        Platform.runLater(() -> this.addConnectionView(connection));
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        // Remove the control that corresponds with the connection that was removed
        Platform.runLater(() -> {
            final ConnectionView connectionView = findConnectionView(event.getConnection());
            if (connectionView != null) {
                this.connections.getChildren().remove(connectionView);
                this.eventBus.unregister(connectionView);
            }
        });
    }
}

