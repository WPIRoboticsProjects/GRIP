package edu.wpi.grip.ui.preview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;

/**
 * A custom implementation of an image view that resizes to fit its parent container.
 */
public class ResizableImageView extends Region {

  private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");
  private final DoubleProperty ratio = new SimpleDoubleProperty(this, "ratio", 1);
  private static final BackgroundSize size =
      new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false);

  /**
   * Creates a new resizable image view.
   */
  public ResizableImageView() {
    super();

    getStyleClass().add("resizable-image");

    image.addListener((obs, old, img) -> {
      if (img == null) {
        setBackground(null);
        ratio.set(1);
      } else if (img != old) {
        // Only create a new background object when the image changes
        // Otherwise we would be creating a new background object for every frame of every preview
        Background background = createImageBackground(img);
        setBackground(background);
        ratio.set(img.getWidth() / img.getHeight());
        setPrefHeight(img.getHeight());
        setPrefWidth(USE_COMPUTED_SIZE);
      }
    });
  }

  /**
   * Creates a background that displays only the given image.
   *
   * @param img the image to create the background for
   */
  private static Background createImageBackground(Image img) {
    BackgroundImage backgroundImage = new BackgroundImage(
        img,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        null,
        size
    );
    return new Background(backgroundImage);
  }

  public Image getImage() {
    return image.get();
  }

  public ObjectProperty<Image> imageProperty() {
    return image;
  }

  public void setImage(Image image) {
    this.image.set(image);
  }

  public double getRatio() {
    return ratio.get();
  }

  public ReadOnlyDoubleProperty ratioProperty() {
    return ratio;
  }

  @Override
  public Orientation getContentBias() {
    return Orientation.VERTICAL;
  }

  /**
   * Computes the width of the displayed image for the given target height, maintaining the image's
   * intrinsic aspect ratio.
   *
   * @param height the target height of the image
   * @return the width of the image
   */
  private double computeImageWidthForHeight(double height) {
    if (getImage() == null) {
      return 1;
    }
    return height * getRatio();
  }

  @Override
  protected double computePrefWidth(double height) {
    return computeImageWidthForHeight(height);
  }
}
