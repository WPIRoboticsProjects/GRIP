package edu.wpi.grip.core.cuda;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_cudaarithm;

/**
 * Detects CUDA installs on Windows.
 */
public class WindowsCudaDetector implements CudaDetector {

  private boolean hasCuda;
  private boolean checkedForCuda = false;

  @Override
  public boolean isCompatibleCudaInstalled() {
    // Note: since CUDA isn't necessarily going to be installed in the default location
    // (though it is likely), it's easiest for us to just check if an OpenCV CUDA class
    // can have its JNI loaded, since it'll require the CUDA DLLs to also be available.
    // We cache the value so it's only checked once, since installing CUDA while GRIP's
    // running will require a system reboot anyway to set the PATH environment variable
    if (!checkedForCuda) {
      try {
        Loader.load(opencv_cudaarithm.class);
        hasCuda = true;
      } catch (UnsatisfiedLinkError | NoClassDefFoundError error) {
        // Couldn't load the JNI, no compatible CUDA runtime is available
        hasCuda = false;
      }
      checkedForCuda = true;
    }

    return hasCuda;
  }
}
