package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;

import java.util.Optional;

/**
 * An {@link InputSocketView} for booleans that shows a checkbox for the user to turn on or off
 */
public class CheckboxInputSocketView extends InputSocketView<Boolean> {

    private final CheckBox checkBox;

    public CheckboxInputSocketView(EventBus eventBus, InputSocket<Boolean> socket) {
        super(eventBus, socket);

        this.checkBox = new CheckBox();
        assignSocketValue(socket.getValue());
        this.checkBox.selectedProperty().addListener(o -> this.getSocket().setValue(this.checkBox.isSelected()));
        this.checkBox.disableProperty().bind(this.getHandle().connectedProperty());

        // Checkboxes are small enough that they can just be displayed to the right of the ID labels instead of on a new row.
        this.getIdentifier().setGraphic(this.checkBox);
        this.getIdentifier().setContentDisplay(ContentDisplay.RIGHT);
    }

    private void assignSocketValue(final Optional<Boolean> value) {
        this.checkBox.setSelected(value.isPresent() && value.get());
    }

    @Subscribe
    public void updateCheckboxFromSocket(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            assignSocketValue(event.getSocket().getValue());
        }
    }
}
