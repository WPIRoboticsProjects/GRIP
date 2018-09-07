package edu.wpi.grip.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for files that may be used in tests
 */
public class Files {
  public static final ImageWithData IMAGE_FILE;
  public static final ImageWithData GOMPEI_FILE;
  public static final File TEXT_FILE;
  public static final URI TEST_PHOTO_URI;
  public static final URI TEST_PROJECT_URI;


  static {
    try {
      TEXT_FILE = new File(Files.class.getResource("/edu/wpi/grip/images/NotAnImage.txt").toURI());
      IMAGE_FILE = new ImageWithData(new File(
          Files.class.getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI()), 183, 480);
      GOMPEI_FILE = new ImageWithData(new File(
          Files.class.getResource("/edu/wpi/grip/images/gompei.jpeg").toURI()), 220, 225);
      TEST_PHOTO_URI = Files.class.getResource("/edu/wpi/grip/images/testphoto.png").toURI();
      TEST_PROJECT_URI = Files.class.getResource("/edu/wpi/grip/projects/testALL.grip").toURI();

    } catch (URISyntaxException e) {
      throw new IllegalStateException("Could not load file from system", e);
    }
  }
}
