package edu.wpi.grip.core.metrics;

import java.util.AbstractQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A queue implementation that, once full, removes the oldest element before adding a new one.
 */
public class FixedSizeQueue<E> extends AbstractQueue<E> {

  private final int limit;
  private final E[] elements;
  private int currentSize = 0;

  /**
   * Creates a new fixed size queue with the given size limit.
   *
   * @param limit the limit to how many elements may be contained in the queue
   */
  @SuppressWarnings("unchecked")
  public FixedSizeQueue(int limit) {
    checkArgument(limit > 0, "Limit must be a positive integer");
    this.limit = limit;
    this.elements = (E[]) new Object[limit];
  }

  /**
   * Gets the limit to the number of elements in this queue. Once the number of elements in the
   * queue reaches this, adding new elements will remove the oldest elements to make space.
   *
   * @return the maximum limit to the number of elements in this queue
   */
  public int getLimit() {
    return limit;
  }

  @Override
  public Iterator<E> iterator() {
    return new FixedSizeQueueIterator();
  }

  @Override
  public int size() {
    return currentSize;
  }

  @Override
  public boolean offer(E e) {
    if (currentSize < limit) {
      // Haven't hit the limit yet
      elements[currentSize] = e;
      currentSize++;
    } else {
      // Hit the limit, shift everything left (this removes the oldest element)
      shift();
      elements[currentSize - 1] = e;
    }
    return true;
  }

  @Override
  public E poll() {
    if (currentSize == 0) {
      return null;
    }
    E e = elements[currentSize - 1];
    currentSize--;
    return e;
  }

  @Override
  public E peek() {
    if (currentSize == 0) {
      return null;
    }
    return elements[currentSize - 1];
  }

  /**
   * Shifts elements left by one. This replaces the oldest element with the next-oldest one.
   */
  private void shift() {
    System.arraycopy(elements, 1,
        elements, 0, limit - 1);
  }

  private final class FixedSizeQueueIterator implements Iterator<E> {

    // take a snapshot to avoid concurrent modification
    private final int sizeSnapshot = currentSize;
    private int i = -1;

    @Override
    public boolean hasNext() {
      final boolean hasNext = currentSize > 0 && i < currentSize - 1;
      if (hasNext && currentSize != sizeSnapshot) {
        throw new ConcurrentModificationException();
      }
      return hasNext;
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return elements[++i];
    }
  }

}
