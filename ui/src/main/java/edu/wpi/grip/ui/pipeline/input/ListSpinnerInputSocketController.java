package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.util.GRIPPlatform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;

/**
 * An {@link InputSocketController} that lets the user set the value with a {@link Spinner}
 */
public class ListSpinnerInputSocketController extends InputSocketController<List> {

    private final SpinnerValueFactory<List> valueFactory;
    private final InvalidationListener updateSocketFromSpinner;
    private final GRIPPlatform platform;

    public interface Factory {
        ListSpinnerInputSocketController create(InputSocket<List> socket);
    }

    /**
     * @param socket an input socket where the domain contains all of the possible values to choose from
     */
    @Inject
    ListSpinnerInputSocketController(EventBus eventBus, SocketHandleView.Factory socketHandleViewFactory, GRIPPlatform platform, @Assisted InputSocket<List> socket) {
        super(socketHandleViewFactory, socket);
        this.platform = platform;

        final Object[] domain = socket.getSocketHint().getDomain().get();

        @SuppressWarnings("unchecked")
        ObservableList<List> domainList = (ObservableList) FXCollections.observableList(Arrays.asList(domain));

        this.valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(domainList);
        this.updateSocketFromSpinner = o -> this.getSocket().setValue(this.valueFactory.getValue());
        this.valueFactory.setValue(socket.getValue().get());
        this.valueFactory.valueProperty().addListener(this.updateSocketFromSpinner);
    }

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        final Spinner<List> spinner = new Spinner<>(this.valueFactory);
        spinner.setEditable(true);
        spinner.disableProperty().bind(this.getHandle().connectedProperty());
        spinner.focusedProperty().addListener((s, ov, nv) -> {// Code found at http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
            if (nv) return;
            commitEditorText(spinner);
        });
        this.setContent(spinner);
    }


    @Subscribe
    public void updateSpinnerFromSocket(SocketChangedEvent event) {
        if (event.isRegarding(this.getSocket())) {
            platform.runAsSoonAsPossible(() -> {
                // Remove the invalidation listener when we set the value.  This listener is useful for updating the socket value
                // when the user changes the spinner, but since we're setting the spinner value from the socket value, calling it
                // here would not only be redundant, but would create an infinite loop.
                synchronized (this.valueFactory) {
                    this.valueFactory.valueProperty().removeListener(updateSocketFromSpinner);
                    this.valueFactory.setValue(this.getSocket().getValue().get());
                    this.valueFactory.valueProperty().addListener(updateSocketFromSpinner);
                }
            });
        }
    }

    // Code found at http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }
}
