package edu.wpi.grip.ui;

import org.pegdown.PegDownProcessor;

import javafx.scene.control.Alert;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An alert that warns the user when they try to do something unsupported, such as trying to
 * generate code with an operation that doesn't support code gen.
 *
 * <p>The alert renders the body text as markdown.
 * </p>
 */
public class WarningAlert extends Alert {

  /**
   * Markdown processor. Converts markdown to HTML.
   */
  private static final PegDownProcessor mdProcessor = new PegDownProcessor();
  private final WebView webView = new WebView();
  private final WebEngine webEngine = webView.getEngine();

  // Width and height of the alert dialog
  private static final double WIDTH = 360;
  private static final double HEIGHT = 180;

  /**
   * Creates a new warning alert.
   *
   * @param header the header text of the alert. This should be short and descriptive.
   * @param body   the body text of the alert. This supports markdown formatting.
   * @param owner  the owner window of this alert
   *
   * @throws NullPointerException if any of the parameters are null
   */
  public WarningAlert(String header, String body, Window owner) {
    super(AlertType.WARNING);
    checkNotNull(header, "The header text cannot be null");
    checkNotNull(body, "The body text cannot be null");
    checkNotNull(owner, "The owner window cannot be null");

    initStyle(StageStyle.UNDECORATED);
    initOwner(owner);
    initWebView();

    setHeaderText(header);
    webEngine.loadContent(mdProcessor.markdownToHtml(body));
    getDialogPane().setContent(webView);
  }

  /**
   * Initializes the web view and engine prior to setting its content.
   */
  private void initWebView() {
    webView.setBlendMode(BlendMode.DARKEN); // make the background transparent
    webEngine.setUserStyleSheetLocation(
        getClass().getResource("warning_alert.css").toExternalForm()
    );
    webView.setPrefSize(WIDTH, HEIGHT);
  }

}
