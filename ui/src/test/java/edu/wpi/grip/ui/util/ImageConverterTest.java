package edu.wpi.grip.ui.util;

import edu.wpi.grip.core.util.ImageLoadingUtility;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.ImageWithData;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.junit.Assert.assertEquals;


public class ImageConverterTest extends ApplicationTest {
  private static final ImageWithData gompeiImage = Files.gompeiJpegFile;
  private static final ImageWithData imageFile = Files.imageFile;

  private ImageConverter converter;

  @Override
  public void start(Stage stage) {
    converter = new ImageConverter();
    stage.setScene(new Scene(new Pane()));
    stage.show();
  }

  @Test
  public void testConvertImage() throws Exception {
    Mat mat = new Mat();
    ImageLoadingUtility.loadImage(
        URLDecoder.decode(Paths.get(gompeiImage.file.toURI()).toString(),
            StandardCharsets.UTF_8.name()), mat);
    interact(() -> {
      Image javaFXImage = converter.convert(mat);
      assertSameImage(gompeiImage, javaFXImage);
    });

  }

  @Test(expected = IllegalStateException.class)
  public void testConvertInWrongThreadThrowsIllegalState() {
    converter.convert(new Mat());
  }

  @Test
  public void testConvertImageSwitch() throws Exception {
    Mat gompeiMat = new Mat();
    Mat imageMat = new Mat();
    ImageLoadingUtility.loadImage(
        URLDecoder.decode(Paths.get(gompeiImage.file.toURI()).toString(),
            StandardCharsets.UTF_8.name()), gompeiMat);
    ImageLoadingUtility.loadImage(
        URLDecoder.decode(Paths.get(imageFile.file.toURI()).toString(),
            StandardCharsets.UTF_8.name()), imageMat);
    interact(() -> {
      converter.convert(gompeiMat);
      Image javaFXImage = converter.convert(imageMat);
      assertSameImage(imageFile, javaFXImage);
    });
  }

  @Test
  public void testConvertSingleChanelImage() throws Exception {
    final Mat gompeiMat = new Mat();
    ImageLoadingUtility.loadImage(
        URLDecoder.decode(Paths.get(gompeiImage.file.toURI()).toString(),
            StandardCharsets.UTF_8.name()), gompeiMat);
    final Mat desaturatedMat = new Mat();
    cvtColor(gompeiMat, desaturatedMat, COLOR_BGR2GRAY);

    interact(() -> {
      Image javaFXImage = converter.convert(desaturatedMat);
      assertSameImage(gompeiImage, javaFXImage);
    });
  }

  private void assertSameImage(ImageWithData imageWithData, Image javaFXImage) {
    assertEquals("Image was not the same", imageWithData.getCols(),
        Math.round(javaFXImage.getWidth()));
    assertEquals("Image was not the same", imageWithData.getRows(),
        Math.round(javaFXImage.getHeight()));
  }
}
