package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A JavaFX control that represents a {@link Source}.  <code>SourceView</code>s are somewhat analogous to
 * {@link StepView}s in thatthe pipeline contrains them and they contain some sockets, but <code>SourceView</code>s
 * only have output sockets, and they show up in a different place.
 */
public class SourceView extends VBox {

    private final EventBus eventBus;
    private final Source source;

    private final VBox sockets;

    public SourceView(EventBus eventBus, Source source) {
        this.eventBus = eventBus;
        this.source = source;

        this.getStyleClass().add("source");

        final Label nameLabel = new Label(source.getName());
        nameLabel.getStyleClass().add("source-name");
        this.getChildren().add(nameLabel);

        this.sockets = new VBox();
        this.getChildren().add(this.sockets);

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
}
