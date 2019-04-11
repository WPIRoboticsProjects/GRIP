package edu.wpi.grip.core;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.GpuMat;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.Objects;
import java.util.function.Function;

import static org.bytedeco.javacpp.opencv_core.CV_16S;
import static org.bytedeco.javacpp.opencv_core.CV_16U;
import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.CV_32S;
import static org.bytedeco.javacpp.opencv_core.CV_64F;
import static org.bytedeco.javacpp.opencv_core.CV_8S;
import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_16S;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_16U;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_1U;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_32F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_32S;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_64F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8S;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

/**
 * Wraps a GPU mat and a CPU mat and allows device memory and host memory
 * to be used semi-transparently. A wrapper may change between wrapping an image in host memory or
 * an image in GPU memory. A wrapper is used to minimize copies between host and device memory,
 * which may take longer than the time savings of using a CUDA-accelerated operation.
 *
 * <p>Data is lazily copied between host and device memory when needed. Wrappers that
 * are only accessed from CPU operations will never have their data stored in device memory.
 * Accessing a wrapper from CUDA land with {@link #getGpu()} will first copy the existing data from
 * host memory to device, if the wrapper was most recently accessed from CPU code (from either
 * {@link #getCpu()} or {@link #rawCpu()}). This behavior also applies in the reverse, for wrappers
 * accessed from CPU land when they have been most recently used from CUDA code.
 */
@SuppressWarnings("PMD.GodClass")
public final class MatWrapper {

  private final Mat cpuMat;
  private final GpuMat gpuMat;

  /**
   * Flags whether or not the wrapped data is in host memory. Data may still be available in device
   * memory, but the value in host memory will be the working version.
   */
  private boolean isCpu = true;

  /**
   * Flags whether or not the wrapped value has been modified since the most recent read. This
   * is used so that the value is only copied between host and device when it's needed.
   */
  private boolean changed = false;

  /**
   * Creates an empty wrapper. Both mats are empty and the wrapper is treated as a CPU mat.
   */
  public static MatWrapper emptyWrapper() {
    return new MatWrapper();
  }

  /**
   * Creates a wrapper around an existing mat in host memory.
   */
  public static MatWrapper wrap(Mat cpuMat) {
    return new MatWrapper(cpuMat);
  }

  /**
   * Creates a wrapper around an existing mat in GPU memory.
   */
  public static MatWrapper wrap(GpuMat gpuMat) {
    return new MatWrapper(gpuMat);
  }

  /**
   * Creates a wrapper around existing mats in host and device memory.
   *
   * @param cpuMat the mat in host memory to use
   * @param gpuMat the mat in device memory to use
   */
  public static MatWrapper using(Mat cpuMat, GpuMat gpuMat) {
    return new MatWrapper(cpuMat, gpuMat);
  }

  private MatWrapper() {
    this.cpuMat = new Mat();
    this.gpuMat = new GpuMat();
  }

  /**
   * Creates a wrapper for a CPU mat. The mat may be accessed with {@link #getCpu()}.
   */
  private MatWrapper(Mat cpuMat) {
    Objects.requireNonNull(cpuMat, "cpuMat");
    this.cpuMat = cpuMat;
    this.gpuMat = new GpuMat();
  }

  /**
   * Creates a wrapper for a GPU mat. The mat may be accessed with {@link #getGpu()}
   */
  private MatWrapper(GpuMat gpuMat) {
    Objects.requireNonNull(gpuMat, "gpuMat");
    this.cpuMat = new Mat();
    this.gpuMat = gpuMat;
    this.isCpu = false;
  }

  private MatWrapper(Mat cpuMat, GpuMat gpuMat) {
    Objects.requireNonNull(cpuMat, "cpuMat");
    Objects.requireNonNull(gpuMat, "gpuMat");
    this.cpuMat = cpuMat;
    this.gpuMat = gpuMat;
  }

  /**
   * Checks if this is a wrapper around a CPU mat.
   */
  public boolean isCpu() {
    return isCpu;
  }

  /**
   * Checks if this is a wrapper around a GPU mat.
   */
  public boolean isGpu() {
    return !isCpu;
  }

  /**
   * Gets the raw CPU mat. This should only be used when this mat is used as a {@code dst} parameter
   * to an OpenCV function. If you want to get the current value as a mat in host memory, use
   * {@link #getCpu()}.
   */
  public Mat rawCpu() {
    // Assume the mat is about to be modified as a `dst` parameter to an OpenCV function
    // running on the CPU
    isCpu = true;
    changed = true;
    return cpuMat;
  }

  /**
   * Gets the raw GPU mat. This should only be used when this mat is used as a {@code dst} parameter
   * to an OpenCV function. If you want to get the current value as a mat in GPU memory, use
   * {@link #getGpu()}.
   */
  public GpuMat rawGpu() {
    // Assume the mat is about to be modified as a `dst` parameter to an OpenCV function
    // running on a CUDA device
    isCpu = false;
    changed = true;
    return gpuMat;
  }

  /**
   * Gets this mat as a mat in host memory. If this is {@link #isGpu() backed by GPU memory}, the
   * device memory will be copied into the CPU mat before being returned. This copy only happens
   * after {@link #set(GpuMat) set(GpuMat)} is called, and only once between successive calls;
   * invocations of this method after the first copy will not perform another.
   */
  public Mat getCpu() {
    if (changed && !isCpu) {
      gpuMat.download(cpuMat);
      changed = false;
    }
    return cpuMat;
  }

  /**
   * Gets this mat as a mat in GPU memory. If this is {@link #isCpu() backed by host memory}, the
   * host memory will be copied into the GPU mat before being returned. This copy only happens
   * after {@link #set(Mat) set(Mat)} is called, and only once between successive calls;
   * invocations of this method after the first copy will not perform another.
   */
  public GpuMat getGpu() {
    if (changed && isCpu) {
      gpuMat.upload(cpuMat);
      changed = false;
    }
    return gpuMat;
  }

  /**
   * Sets this as being backed by an image in host memory. The data in the given mat will be copied
   * into the internal CPU mat, but not the GPU mat until {@link #getGpu()} is called. This avoids
   * unnecessary memory copies between GPU and host memory.
   */
  public void set(Mat mat) {
    mat.copyTo(cpuMat);
    isCpu = true;
    changed = true;
  }

  /**
   * Sets this as being backed by an image in GPU memory. The data in the given mat will be copied
   * into the internal GPU mat, but not the mat residing in host memory until {@link #getCpu()} is
   * called. This avoids unnecessary memory copies between GPU and host memory.
   */
  public void set(GpuMat mat) {
    gpuMat.put(mat);
    isCpu = false;
    changed = true;
  }

  /**
   * Sets this as being backed by the given wrapper. This wrapper will be functionally equivalent
   * to the one given.
   */
  public void set(MatWrapper wrapper) {
    if (wrapper.isCpu()) {
      set(wrapper.cpuMat);
    } else {
      set(wrapper.gpuMat);
    }
  }

  /**
   * Copies the data of this wrapper to a mat in host memory.
   */
  public void copyTo(Mat mat) {
    if (isCpu) {
      cpuMat.copyTo(mat);
    } else {
      gpuMat.download(mat);
    }
  }

  /**
   * Copies the data of this wrapper to a mat in GPU memory.
   */
  public void copyTo(GpuMat mat) {
    if (isCpu) {
      mat.upload(cpuMat);
    } else {
      mat.put(gpuMat);
    }
  }

  /**
   * Copies the data of this wrapper into another. Equivalent to {@code wrapper.set(this)}
   */
  public void copyTo(MatWrapper wrapper) {
    wrapper.set(this);
  }

  /**
   * Extracts a property shared by both Mats and GpuMats. Unfortunately, they don't share a common
   * API, so we have to do something like this.
   * <p>
   * Example use:
   * <pre><code>
   * Size size = extract(Mat::size, GpuMat::size);
   * </code></pre>
   * </p>
   *
   * @param ifCpu the function to call if this is backed by a mat in host memory
   * @param ifGpu the function to call if this is backed by a mat in GPU memory
   * @param <T>   the type of the property to extract
   */
  private <T> T extract(Function<Mat, T> ifCpu, Function<GpuMat, T> ifGpu) {
    if (isCpu) {
      return ifCpu.apply(cpuMat);
    } else {
      return ifGpu.apply(gpuMat);
    }
  }

  /**
   * Gets the number of columns in this image.
   */
  public int cols() {
    return extract(Mat::cols, GpuMat::cols);
  }

  /**
   * Gets the number of rows in this image.
   */
  public int rows() {
    return extract(Mat::rows, GpuMat::rows);
  }

  /**
   * Gets the type of the data format of this image.
   */
  public int type() {
    return extract(Mat::type, GpuMat::type);
  }

  /**
   * Gets the number of color channels in this image.
   */
  public int channels() {
    return extract(Mat::channels, GpuMat::channels);
  }

  /**
   * Gets the channel depth of this image.
   */
  public int depth() {
    return extract(Mat::depth, GpuMat::depth);
  }

  /**
   * Checks if this image is empty.
   */
  public boolean empty() {
    return extract(Mat::empty, GpuMat::empty);
  }

  /**
   * Gets the size (width by height) of this image.
   */
  public Size size() {
    return extract(Mat::size, GpuMat::size);
  }

  /**
   * Gets the maximum possible value able to be held as a single element in this image.
   */
  @SuppressWarnings("PMD")
  public double highValue() {
    return extract(Mat::highValue, g -> {
      double highValue = 0.0;
      switch (arrayDepth(g)) {
        case IPL_DEPTH_8U:
          highValue = 0xFF;
          break;
        case IPL_DEPTH_16U:
          highValue = 0xFFFF;
          break;
        case IPL_DEPTH_8S:
          highValue = Byte.MAX_VALUE;
          break;
        case IPL_DEPTH_16S:
          highValue = Short.MAX_VALUE;
          break;
        case IPL_DEPTH_32S:
          highValue = Integer.MAX_VALUE;
          break;
        case IPL_DEPTH_1U:
        case IPL_DEPTH_32F:
        case IPL_DEPTH_64F:
          highValue = 1.0;
          break;
        default:
          assert false;
      }
      return highValue;
    });
  }

  private static int arrayDepth(GpuMat m) {
    switch (m.depth()) {
      case CV_8U:
        return IPL_DEPTH_8U;
      case CV_8S:
        return IPL_DEPTH_8S;
      case CV_16U:
        return IPL_DEPTH_16U;
      case CV_16S:
        return IPL_DEPTH_16S;
      case CV_32S:
        return IPL_DEPTH_32S;
      case CV_32F:
        return IPL_DEPTH_32F;
      case CV_64F:
        return IPL_DEPTH_64F;
      default:
        throw new UnsupportedOperationException("Unsupported depth " + m.depth());
    }
  }

  /**
   * Allocates new array data if needed.
   *
   * @param rows New number of rows.
   * @param cols New number of columns.
   * @param type New matrix type.
   */
  public void create(int rows, int cols, int type) {
    if (isCpu) {
      cpuMat.create(rows, cols, type);
    } else {
      gpuMat.create(rows, cols, type);
    }
    changed = true;
  }

  /**
   * Sets all or some of the array elements to the specified value.
   *
   * @param value Assigned scalar converted to the actual array type.
   */
  public MatWrapper put(opencv_core.Scalar value) {
    if (isCpu()) {
      cpuMat.put(value);
    } else {
      gpuMat.setTo(value);
    }
    changed = true;
    return this;
  }
}
