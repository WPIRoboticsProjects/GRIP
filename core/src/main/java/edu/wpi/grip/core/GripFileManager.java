package edu.wpi.grip.core;

import com.google.common.io.Files;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class GripFileManager implements FileManager {

    private static final Logger logger = Logger.getLogger(GripFileManager.class.getName());

    private final File gripDirectory;
    private final File imageDirectory;

    public GripFileManager() {
        gripDirectory = new File(System.getProperty("user.home") + File.separator + "GRIP");
        imageDirectory = new File(gripDirectory, "images");
    }

    @Override
    public void saveImage(byte[] image, String fileName) {
        File file = new File(imageDirectory, fileName);
        Runnable runnable = () -> {
            try {
                imageDirectory.mkdirs();
                Files.write(image, file);
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        };
        new Thread(runnable).start();
    }
}
