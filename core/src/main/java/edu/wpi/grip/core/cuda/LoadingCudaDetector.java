package edu.wpi.grip.core.cuda;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_cudaarithm;

/**
 * Checks if CUDA is available by attempting to load one of the OpenCV CUDA class' JNI. If the JNI
 * cannot be loaded, then no compatible CUDA runtime is available. This approach is probably the
 * most flexible; it's OS-agnostic, since it lets the JVM handle loading the JNI libraries and
 * linking, and doesn't require knowledge of CUDA installation locations - it just needs to be on
 * the PATH.
 *
 * <p>Only one attempt is made to load the JNI, and the result is cached. Any later calls to
 * {@link #isCompatibleCudaInstalled()} will simply return the cached value.
 */
public class LoadingCudaDetector implements CudaDetector {

  private volatile boolean hasCuda = false;
  private volatile boolean checkedForCuda = false;

  @Override
  public boolean isCompatibleCudaInstalled() {
    if (!checkedForCuda) {
      try {
        Loader.load(opencv_cudaarithm.class);
        hasCuda = true;
      } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
        // Couldn't load the JNI, no compatible CUDA runtime is available
        hasCuda = false;
      }
      checkedForCuda = true;
    }

    return hasCuda;
  }
}
