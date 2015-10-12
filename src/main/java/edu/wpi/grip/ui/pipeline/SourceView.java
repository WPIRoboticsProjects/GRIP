package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * A JavaFX control that represents a {@link Source}.  <code>SourceView</code>s are somewhat analogous to
 * {@link StepView}s in thatthe pipeline contrains them and they contain some sockets, but <code>SourceView</code>s
 * only have output sockets, and they show up in a different place.
 */
public class SourceView extends VBox {

    @FXML
    private Label name;

    @FXML
    private VBox sockets;

    private final EventBus eventBus;
    private final Source source;

    public SourceView(EventBus eventBus, Source source) {
        this.eventBus = eventBus;
        this.source = source;

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

    public Source getSource() {
        return this.source;
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
