package edu.wpi.grip.core;

/**
 * Defines what methods a FileManager implementation must implement.
 */
public interface FileManager {

    /**
     * Saves an array of bytes to a file.
     *
     * @param image The image to save
     * @param fileName The file name to save
     */
    void saveImage(byte[] image, String fileName);
}
