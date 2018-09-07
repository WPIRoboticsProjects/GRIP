package edu.wpi.grip.core.util;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class to make JavaCPP objects and containers easier to work with.
 */
public final class JavaCppUtils {

  private static final int VECTOR_STREAM_CHARACTERISTICS
      = Spliterator.SIZED
      | Spliterator.IMMUTABLE
      | Spliterator.DISTINCT
      | Spliterator.ORDERED;

  private JavaCppUtils() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  /**
   * Helper method for streaming an arbitrary native vector.
   *
   * @param size   the number of elements in the vector
   * @param get    a function to get an element at a specific index
   * @param <E>    the type of the elements in the vector
   * @param <V>    the type of the vector
   */
  private static <E, V> Stream<E> streamVector(long size, LongFunction<E> get) {
    return StreamSupport.stream(new VectorSpliterator<>(size, get), false);
  }

  /**
   * Streams the {@link Mat} elements of a native {@code MatVector}.
   *
   * @param matVector the vector to stream the elements of
   *
   * @return a stream of the Mats contained in the MatVector
   */
  public static Stream<Mat> stream(MatVector matVector) {
    return streamVector(matVector.size(), matVector::get);
  }

  /**
   * Creates a collector to collect a stream of {@link Mat Mats} to a native {@link MatVector}.
   */
  public static Collector<Mat, ?, MatVector> toMatVector() {
    return new VectorCollector<>(MatVector::new, MatVector::push_back);
  }

  /**
   * Helper method for creating a collector to collect a stream of elements to a native container.
   *
   * @param <E> the type of the elements in the vector
   * @param <V> the vector type
   */
  private static class VectorCollector<E, V> implements Collector<E, List<E>, V> {
    private final Function<Long, V> supplier;
    private final BiConsumer<V, E> adder;

    /**
     * Creates a new collector.
     *
     * @param supplier the supplier function to create a new vector object with a given size
     * @param adder    the function to use to add elements to the vector
     */
    VectorCollector(Function<Long, V> supplier, BiConsumer<V, E> adder) {
      this.supplier = supplier;
      this.adder = adder;
    }

    @Override
    public Supplier<List<E>> supplier() {
      return ArrayList::new;
    }

    @Override
    public BiConsumer<List<E>, E> accumulator() {
      return List::add;
    }

    @Override
    public BinaryOperator<List<E>> combiner() {
      return (a, b) -> {
        a.addAll(b);
        return a;
      };
    }

    @Override
    public Function<List<E>, V> finisher() {
      return list -> {
        V vector = supplier.apply((long) list.size());
        list.forEach(t -> adder.accept(vector, t));
        return vector;
      };
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Set.of();
    }
  }

  private static class VectorSpliterator<E> extends AbstractSpliterator<E> {
    private final AtomicLong index;
    private final long size;
    private final LongFunction<E> get;

    VectorSpliterator(long size, LongFunction<E> get) {
      super(size, JavaCppUtils.VECTOR_STREAM_CHARACTERISTICS);
      this.size = size;
      this.get = get;
      index = new AtomicLong(0);
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
      long i = index.incrementAndGet();
      if (i >= size) {
        return false;
      }
      action.accept(get.apply(i));
      return true;
    }
  }
}
