package edu.wpi.grip.core.events;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when the user should be warned they tried to do something unsupported, such as
 * trying to generate code with an operation that does not support code gen.
 *
 * <p>The event contains a short header text describing the warning and a detailed body text
 * that lets the user know why what they attempted was not allowed.</p>
 */
public class WarningEvent {

  private final String header;
  private final String body;

  /**
   * Creates a new warning event.
   *
   * @param header the header or title of the warning (e.g. "Cannot generate code").
   *               This should be short and descriptive.
   * @param body   the body of the warning.
   *               This should go into detail about what the user did wrong.
   */
  public WarningEvent(String header, String body) {
    checkNotNull(header, "Header text cannot be null");
    checkNotNull(body, "Body text cannot be null");
    this.header = header;
    this.body = body;
  }

  /**
   * Gets the warning header.
   */
  public String getHeader() {
    return header;
  }

  /**
   * Gets the warning body.
   */
  public String getBody() {
    return body;
  }

}
