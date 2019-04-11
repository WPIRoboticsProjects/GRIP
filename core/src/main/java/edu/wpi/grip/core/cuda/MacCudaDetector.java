package edu.wpi.grip.core.cuda;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
      "/Developer/NVIDIA/CUDA-" + REQUIRED_MAJOR_VERSION + "/version.txt";

  @Override
  public boolean isCompatibleCudaInstalled() {
    try {
      return Files.list(Paths.get("/Developer/NVIDIA"))
          .map(Path::toString)
          .filter(p -> p.contains("CUDA-"))
          .anyMatch(p -> p.startsWith("CUDA-" + REQUIRED_MAJOR_VERSION + "."));
    } catch (IOException e) {
      // CUDA install dir does not exist or could not be read
      return false;
    }
  }
}
