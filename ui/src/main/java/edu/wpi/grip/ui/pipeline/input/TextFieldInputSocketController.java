package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import javax.inject.Inject;

/**
 * An {@link InputSocketView} that lets the user set the value of a number with a {@link javafx.scene.control.TextField}
 */
public class TextFieldInputSocketController extends InputSocketController<String> {

    private TextField textField;

    public interface Factory {
        TextFieldInputSocketController create(InputSocket<String> socket);
    }

    @Inject
    TextFieldInputSocketController(SocketHandleView.Factory socketHandleViewFactory,
                                          @Assisted InputSocket<String> socket) {
        super(socketHandleViewFactory, socket);
    }

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        textField = new TextField(getSocket().getValue().get());
        textField.setPromptText(getSocket().getSocketHint().getIdentifier());
        textField.disableProperty().bind(this.getHandle().connectedProperty());
        textField.focusedProperty().addListener(observable -> {
            if (!textField.isFocused()) getSocket().setValue(textField.getText());
        });

        setContent(textField);
    }

    @Subscribe
    public void updateTextFieldFromSocket(SocketChangedEvent event) {
        if (event.isRegarding(getSocket())) {
            final String text = getSocket().getValue().get();
            Platform.runLater(() -> textField.setText(text));
        }
    }
}
