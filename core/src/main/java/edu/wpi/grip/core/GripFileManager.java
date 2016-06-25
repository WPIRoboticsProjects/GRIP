package edu.wpi.grip.core;

import com.google.common.io.Files;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@code FileManager}.  Saves files into a directory named GRIP in the user's home folder.
 */
@Singleton
public class GripFileManager implements FileManager {

    private static final Logger logger = Logger.getLogger(GripFileManager.class.getName());

    private static final File GRIP_DIRECTORY = new File(System.getProperty("user.home") + File.separator + "GRIP");
    private static final File IMAGE_DIRECTORY = new File(GRIP_DIRECTORY, "images");

    @Override
    public void saveImage(byte[] image, String fileName) {
        checkNotNull(image);
        checkNotNull(fileName);

        File file = new File(IMAGE_DIRECTORY, fileName);
        Runnable runnable = () -> {
            try {
                IMAGE_DIRECTORY.mkdirs(); // If the user deletes the directory
                Files.write(image, file);
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        };
        new Thread(runnable).start();
    }
}
