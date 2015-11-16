package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link InputSocketView} that lets the user set the value with a {@link Spinner}
 */
public class ListSpinnerInputSocketView<T> extends InputSocketView<T> {

    private final SpinnerValueFactory<T> valueFactory;
    private final InvalidationListener updateSocketFromSpinner;

    /**
     * @param socket an input socket where the domain contains all of the possible values to choose from
     */
    public ListSpinnerInputSocketView(EventBus eventBus, InputSocket<T> socket) {
        super(eventBus, socket);

        final Object[] domain = socket.getSocketHint().getDomain();
        checkNotNull(domain);

        @SuppressWarnings("unchecked")
        ObservableList<T> domainList = (ObservableList<T>) FXCollections.observableList(Arrays.asList(domain));

        this.valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(domainList);
        this.updateSocketFromSpinner = o -> this.getSocket().setValue(this.valueFactory.getValue());
        this.valueFactory.setValue(socket.getValue());
        this.valueFactory.valueProperty().addListener(this.updateSocketFromSpinner);

        final Spinner<T> spinner = new Spinner<>(this.valueFactory);
        spinner.setEditable(true);
        spinner.disableProperty().bind(this.getHandle().connectedProperty());
        this.setContent(spinner);
    }

    @Subscribe
    public void updateSpinnerFromSocket(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            // Remove the invalidation listener when we set the value.  This listener is useful for updating the socket value
            // when the user changes the spinner, but since we're setting the spinner value from the socket value, calling it
            // here would not only be redundant, but would create an infinite loop.
            synchronized (this.valueFactory) {
                this.valueFactory.valueProperty().removeListener(updateSocketFromSpinner);
                this.valueFactory.setValue(this.getSocket().getValue());
                this.valueFactory.valueProperty().addListener(updateSocketFromSpinner);
            }
        }
    }
}
