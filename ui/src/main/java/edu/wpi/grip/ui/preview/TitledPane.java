package edu.wpi.grip.ui.preview;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Custom implementation of a titled pane. The JavaFX implementation has a tendency to add a gap on
 * the sides of image content when resized.
 */
public class TitledPane extends BorderPane {

  private final Label label = new Label();
  private final HBox top = new HBox(label);
  private final StackPane center = new StackPane();

  private final ObjectProperty<Node> content = new SimpleObjectProperty<>();

  /**
   * Creates a new titled pane with no text and no content.
   */
  public TitledPane() {
    this.getStyleClass().add("titled-pane");
    top.getStyleClass().add("title");
    center.getStyleClass().add("content");

    content.addListener((obs, old, content) -> {
      if (content == null) {
        center.getChildren().clear();
      } else {
        center.getChildren().setAll(content);
      }
    });

    setTop(top);
    setCenter(center);
    setMaxHeight(USE_PREF_SIZE);
  }

  public String getText() {
    return label.getText();
  }

  public void setText(String text) {
    label.setText(text);
  }

  public StringProperty textProperty() {
    return label.textProperty();
  }

  public Node getContent() {
    return content.get();
  }

  public void setContent(Node content) {
    this.content.set(content);
  }

  public ObjectProperty<Node> contentProperty() {
    return content;
  }
}
