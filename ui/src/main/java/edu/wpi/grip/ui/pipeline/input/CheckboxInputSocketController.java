package edu.wpi.grip.ui.pipeline.input;

import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.util.GripPlatform;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;

/**
 * An {@link InputSocketController} for booleans that shows a checkbox for the user to turn on or
 * off.
 */
public class CheckboxInputSocketController extends InputSocketController<Boolean> {

  private final CheckBox checkBox;
  private final GripPlatform platform;

  @Inject
  CheckboxInputSocketController(SocketHandleView.Factory socketHandleViewFactory, GripPlatform
      platform, @Assisted InputSocket<Boolean> socket) {
    super(socketHandleViewFactory, socket);
    this.platform = platform;
    this.checkBox = new CheckBox();
  }

  @FXML
  @Override
  public void initialize() {
    super.initialize();
    // Checkboxes are small enough that they can just be displayed to the right of the ID labels
    // instead of on a new row.
    this.getIdentifier().setGraphic(this.checkBox);
    this.getIdentifier().setContentDisplay(ContentDisplay.RIGHT);
    this.setContent(checkBox);

    assignSocketValue(getSocket().getValue());
    // Add the listener after so that setting the initial value doesn't trigger it.
    this.checkBox.selectedProperty().addListener(o -> this.getSocket().setValue(this.checkBox
        .isSelected()));
  }

  private void assignSocketValue(final Optional<Boolean> value) {
    this.checkBox.setSelected(value.isPresent() && value.get());
  }

  @Subscribe
  public void updateCheckboxFromSocket(SocketChangedEvent event) {
    if (event.isRegarding(getSocket())) {
      platform.runAsSoonAsPossible(() -> assignSocketValue(getSocket().getValue()));
    }
  }

  public interface Factory {
    CheckboxInputSocketController create(InputSocket<Boolean> socket);
  }
}
