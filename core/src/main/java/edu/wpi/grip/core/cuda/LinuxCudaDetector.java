package edu.wpi.grip.core.cuda;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Detects CUDA installs on Linux platforms. Assumes CUDA is installed to {@code /usr/local/cuda/}
 */
@SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME")
public final class LinuxCudaDetector implements CudaDetector {

  private static final String VERSION_FILE_PATH = "/usr/local/cuda/version.txt";

  @Override
  public boolean isCompatibleCudaInstalled() {
    Path path = Paths.get(VERSION_FILE_PATH);
    if (Files.notExists(path)) {
      return false;
    }
    try (BufferedReader reader = Files.newBufferedReader(path)) {
      return checkLine(reader.readLine());
    } catch (IOException e) {
      // File does not exist or could not be read
      return false;
    }
  }

  private static boolean checkLine(String line) {
    if (line == null) {
      return false;
    }
    return line.equals("CUDA Version " + REQUIRED_MAJOR_VERSION)
        || line.startsWith("CUDA Version " + REQUIRED_MAJOR_VERSION + ".");
  }
}
