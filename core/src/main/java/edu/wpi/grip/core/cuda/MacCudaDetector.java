package edu.wpi.grip.core.cuda;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Detects CUDA installs on Mac OS. Assumes CUDA is installed at
 * {@code /Developer/NVIDIA/CUDA-$version/}
 */
@SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME")
public final class MacCudaDetector implements CudaDetector {

  private static final String VERSION_FILE_PATH =
      "/Developer/NVIDIA/CUDA-" + REQUIRED_VERSION + "/version.txt";

  @Override
  public boolean isCompatibleCudaInstalled() {
    Path path = Paths.get(VERSION_FILE_PATH);
    if (Files.notExists(path)) {
      return false;
    }
    try (BufferedReader br = Files.newBufferedReader(path)) {
      return checkLine(br.readLine());
    } catch (IOException e) {
      return false;
    }
  }

  private static boolean checkLine(String line) {
    if (line == null) {
      return false;
    }
    return line.equals("CUDA Version " + REQUIRED_VERSION)
        || line.startsWith("CUDA Version " + REQUIRED_VERSION + ".");
  }
}
