package edu.wpi.grip.ui.pipeline.input;

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
import javafx.scene.control.ChoiceBox;

import java.util.Arrays;

/**
 * An {@link InputSocketController} that shows a drop-down menu containing all of the possible values for the socket
 */
public class SelectInputSocketController<T> extends InputSocketController<T> {

    private final ChoiceBox<T> choiceBox;
    private final InvalidationListener updateSocketFromChoiceBox;
    private final GRIPPlatform platform;

    public interface Factory<T> {
        SelectInputSocketController<T> create(InputSocket<T> socket);
    }

    /**
     * @param socket an input socket where the domain contains all of the possible values to choose from
     */
    @Inject
    SelectInputSocketController(SocketHandleView.Factory socketHandleViewFactory, GRIPPlatform platform, @Assisted InputSocket<T> socket) {
        super(socketHandleViewFactory, socket);
        this.platform = platform;

        final Object[] domain = socket.getSocketHint().getDomain().get();

        @SuppressWarnings("unchecked")
        ObservableList<T> domainList = (ObservableList<T>) FXCollections.observableList(Arrays.asList(domain));

        this.choiceBox = new ChoiceBox<>(domainList);
        this.choiceBox.setValue(socket.getValue().get());
        this.updateSocketFromChoiceBox = o -> this.getSocket().setValue(this.choiceBox.getValue());
    }

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        this.choiceBox.disableProperty().bind(this.getHandle().connectedProperty());

        this.choiceBox.valueProperty().addListener(this.updateSocketFromChoiceBox);

        this.setContent(this.choiceBox);

    }

    @Subscribe
    public void updateChoiceBoxFromSocket(SocketChangedEvent event) {
        if (event.isRegarding(this.getSocket())) {
            platform.runAsSoonAsPossible(() -> {
                // Remove the invalidation listener when we set the value.  This listener is useful for updating the socket value
                // when the user changes the spinner, but since we're setting the spinner value from the socket value, calling it
                // here would not only be redundant, but would create an infinite loop.
                synchronized (this.choiceBox) {
                    this.choiceBox.valueProperty().removeListener(updateSocketFromChoiceBox);
                    this.choiceBox.setValue(this.getSocket().getValue().get());
                    this.choiceBox.valueProperty().addListener(updateSocketFromChoiceBox);
                }
            });
        }
    }
}
