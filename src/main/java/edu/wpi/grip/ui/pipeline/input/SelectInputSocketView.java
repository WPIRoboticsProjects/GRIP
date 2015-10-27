package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link InputSocketView} that shows a drop-down menu containing all of the possible values for the socket
 */
public class SelectInputSocketView<T> extends InputSocketView<T> {

    private final ChoiceBox<T> choiceBox;
    private final InvalidationListener updateSocketFromChoiceBox;

    /**
     * @param socket an input socket where the domain contains all of the possible values to choose from
     */
    public SelectInputSocketView(EventBus eventBus, InputSocket<T> socket) {
        super(eventBus, socket);

        final Object[] domain = socket.getSocketHint().getDomain();
        checkNotNull(domain);

        @SuppressWarnings("unchecked")
        ObservableList<T> domainList = (ObservableList<T>) FXCollections.observableList(Arrays.asList(domain));

        this.choiceBox = new ChoiceBox<>(domainList);
        this.choiceBox.setValue(socket.getValue());
        this.choiceBox.disableProperty().bind(this.getHandle().connectedProperty());

        this.updateSocketFromChoiceBox = o -> this.getSocket().setValue(this.choiceBox.getValue());
        this.choiceBox.valueProperty().addListener(this.updateSocketFromChoiceBox);

        this.setContent(this.choiceBox);
    }

    @Subscribe
    public void updateChoiceBoxFromSocket(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            // Remove the invalidation listener when we set the value.  This listener is useful for updating the socket value
            // when the user changes the spinner, but since we're setting the spinner value from the socket value, calling it
            // here would not only be redundant, but would create an infinite loop.
            synchronized (this.choiceBox) {
                this.choiceBox.valueProperty().removeListener(updateSocketFromChoiceBox);
                this.choiceBox.setValue(this.getSocket().getValue());
                this.choiceBox.valueProperty().addListener(updateSocketFromChoiceBox);
            }
        }
    }
}
