package edu.wpi.grip.core.util;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;

/**
 * Utility class for streaming native vector wrappers like {@code MatVector}
 * ({@code std::vector<T>}) with the Java {@link Stream} API.
 */
public final class PointerStream {

  /**
   * Creates a stream of {@code Mat} objects in a {@code MatVector}.
   *
   * @param vector the vector of {@code Mats} to stream
   *
   * @return a new stream object for the contents of the vector
   */
  public static Stream<Mat> ofMatVector(MatVector vector) {
    return LongStream.range(0, vector.size())
        .mapToObj(vector::get);
  }
}
