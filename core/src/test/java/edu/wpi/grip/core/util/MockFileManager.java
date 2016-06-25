package edu.wpi.grip.core.util;

import edu.wpi.grip.core.FileManager;

public class MockFileManager implements FileManager {

  @Override
  public void saveImage(byte[] image, String fileName) {
    // No body here because this is for testing only.
  }

}
