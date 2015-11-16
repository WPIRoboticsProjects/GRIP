package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple JavaFX container that automatically shows previews of all sockets marked as "previewed".
 *
 * @see OutputSocket#isPreviewed()
 */
public class PreviewsView extends VBox {

    @FXML
    private HBox previewBox;

    private final EventBus eventBus;
    private final List<OutputSocket<?>> previewedSockets;

    public PreviewsView(EventBus eventBus) {
        checkNotNull(eventBus);

        this.eventBus = eventBus;
        this.previewedSockets = new ArrayList<>();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Previews.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.eventBus.register(this);
    }

    @Subscribe
    public synchronized void onSocketPreviewChanged(SocketPreviewChangedEvent event) {
        Platform.runLater(() -> {
            final OutputSocket<?> socket = event.getSocket();

            if (socket.isPreviewed()) {
                // If the socket was just set as previewed, add it to the list of previewed sockets and add a new view for it.
                if (!this.previewedSockets.contains(socket)) {
                    this.previewedSockets.add(socket);
                    this.previewBox.getChildren().add(SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));
                }
            } else {
                // If the socket was just set as not previewed, remove both it and the corresponding control
                int index = this.previewedSockets.indexOf(socket);
                if (index != -1) {
                    this.previewedSockets.remove(index);
                    this.eventBus.unregister(this.previewBox.getChildren().remove(index));
                }
            }
        });
    }
}
