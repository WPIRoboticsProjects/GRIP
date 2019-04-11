package edu.wpi.grip.core.cuda;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Detects CUDA installs on Windows. Assumes CUDA is installed at
 * {@code C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\}
 */
public class WindowsCudaDetector implements CudaDetector {

  /**
   * The default directory into which CUDA is installed.
   */
  private static final String CUDA_DIR = "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA";

  @Override
  public boolean isCompatibleCudaInstalled() {
    try {
      return Files.list(Paths.get(CUDA_DIR))
          .map(Path::getFileName)
          .map(Path::toString)
          .anyMatch(p -> p.startsWith("v" + REQUIRED_MAJOR_VERSION + "."));
    } catch (IOException e) {
      // CUDA dir does not exist or could not be read
      return false;
    }
  }
}
