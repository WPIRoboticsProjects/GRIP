package edu.wpi.grip.ui;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An alert that warns the user when they try to do something unsupported, such as trying to
 * generate code with an operation that does not support code gen.
 */
public class WarningAlert extends Alert {

  /**
   * Creates a new warning alert.
   *
   * @param header the header text of the alert. This should be short and descriptive.
   * @param body   the body text of the alert. This should go into detail about the warning
   *               and what prompted it.
   * @param owner  the owner window of this alert
   *
   * @throws NullPointerException if any of the parameters are null
   */
  public WarningAlert(String header, String body, Window owner) {
    super(AlertType.WARNING);
    checkNotNull(header, "The header text cannot be null");
    checkNotNull(body, "The body text cannot be null");
    checkNotNull(owner, "The owner window cannot be null");

    initStyle(StageStyle.UTILITY);
    initOwner(owner);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // expand to fit content
    getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

    setTitle("Warning | " + header);
    setHeaderText(header);
    setContentText(body);
  }

}
