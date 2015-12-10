package edu.wpi.grip.util;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Utility class for files that may be used in tests
 */
public class Files {
    public static final ImageWithData imageFile, gompeiJpegFile;
    public static final File textFile;
    static {
        try {
            textFile = new File(Files.class.getResource("/edu/wpi/grip/images/NotAnImage.txt").toURI());
            imageFile = new ImageWithData(new File(
                    Files.class.getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI()), 183, 480);
            gompeiJpegFile = new ImageWithData(new File(
                    Files.class.getResource("/edu/wpi/grip/images/gompei.jpeg").toURI()), 220, 225);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not load file from system", e);
        }
    }
}
