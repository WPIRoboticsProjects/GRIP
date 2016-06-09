package edu.wpi.grip.core;

import com.google.common.io.Files;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class GripFileManager implements FileManager {

    private static final Logger logger = Logger.getLogger(GripFileManager.class.getName());

    private static final File gripDirectory = new File(System.getProperty("user.home") + File.separator + "GRIP");
    private static final File imageDirectory = new File(gripDirectory, "images");

    public GripFileManager() {
        gripDirectory.mkdirs();
        imageDirectory.mkdirs();
    }

    @Override
    public void saveImage(byte[] image, String fileName) {
        checkNotNull(image);
        checkNotNull(fileName);

        File file = new File(imageDirectory, fileName);
        Runnable runnable = () -> {
            try {
                imageDirectory.mkdirs(); // If the user deletes the directory
                Files.write(image, file);
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        };
        new Thread(runnable).start();
    }
}
