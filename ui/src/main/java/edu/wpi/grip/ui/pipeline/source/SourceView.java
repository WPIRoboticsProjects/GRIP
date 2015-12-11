package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.ui.pipeline.OutputSocketView;
import edu.wpi.grip.ui.pipeline.StepController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that represents a {@link Source}.  <code>SourceView</code>s are somewhat analogous to
 * {@link StepController}s in thatthe pipeline contrains them and they contain some sockets, but <code>SourceView</code>s
 * only have output sockets, and they show up in a different place.
 *
 * @param <S> The type of Source this view is for.
 */
public abstract class SourceView<S extends Source> extends VBox {

    @FXML
    private Label name;

    @FXML
    private VBox sockets;

    @FXML
    private HBox controls;

    private final EventBus eventBus;
    private final S source;

    public SourceView(EventBus eventBus, S source) {
        this.eventBus = checkNotNull(eventBus, "The EventBus can not be null");
        this.source = checkNotNull(source, "The Source can not be null");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Source.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.name.setText(source.getName());

        for (OutputSocket<?> socket : source.getOutputSockets()) {
            this.sockets.getChildren().add(new OutputSocketView(eventBus, socket));
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
     * @return An unmodifiable list of {@link OutputSocketView}s corresponding to the sockets that this source produces
     */
    @SuppressWarnings("unchecked")
    public ObservableList<OutputSocketView> getOutputSockets() {
        return (ObservableList) this.sockets.getChildrenUnmodifiable();
    }

    @FXML
    public void delete() {
        this.eventBus.post(new SourceRemovedEvent(this.getSource()));
    }

}
