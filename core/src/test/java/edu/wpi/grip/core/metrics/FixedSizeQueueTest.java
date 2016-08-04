package edu.wpi.grip.core.metrics;

import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link FixedSizeQueue}.
 */
public class FixedSizeQueueTest {

  private final FixedSizeQueue<Object> q = new FixedSizeQueue<>(2);

  @Test
  public void getLimit() throws Exception {
    assertEquals("q limit should be 2", 2, q.getLimit());
    assertEquals("q size should be zero", 0, q.size());
  }

  @Test
  public void iterator() throws Exception {
    Iterator<Object> iterator = q.iterator();
    assertFalse("Iterator should be empty", iterator.hasNext());
    q.add("first");
    iterator = q.iterator();
    assertTrue("Iterator should have an element", iterator.hasNext());
    assertEquals("Iterator value should be 'first'", "first", iterator.next());
    assertFalse("Iterator should now be empty", iterator.hasNext());
    q.add("second");
    iterator = q.iterator();
    assertTrue("Iterator should have an element", iterator.hasNext());
    assertEquals("Iterator value should be 'first'", "first", iterator.next());
    assertTrue("Iterator should have another element", iterator.hasNext());
    assertEquals("Iterator value should be 'second'", "second", iterator.next());
    assertFalse("Iterator should now be empty", iterator.hasNext());
    q.add("third");
    iterator = q.iterator();
    assertTrue("Iterator should have an element", iterator.hasNext());
    assertEquals("Iterator value should be 'second'", "second", iterator.next());
    assertTrue("Iterator should have another element", iterator.hasNext());
    assertEquals("Iterator value should be 'third'", "third", iterator.next());
    assertFalse("Iterator should now be empty", iterator.hasNext());
    q.clear();
    iterator = q.iterator();
    try {
      iterator.next();
      fail("Queue is empty, iterator().next() should throw a NoSuchElementException");
    } catch (NoSuchElementException expected) {
      // This is expected, ignore it
    }
  }

  @Test
  public void size() throws Exception {
    assertEquals("queue should be empty", 0, q.size());
    q.add(new Object());
    assertEquals("queue should have one element", 1, q.size());
    q.add(new Object());
    assertEquals("queue should have two elements", 2, q.size());
    q.add(new Object());
    assertEquals("queue should still have two elements", 2, q.size());
  }

  @Test
  public void offer() throws Exception {
    final Object first = new Object();
    final Object second = new Object();
    final Object third = new Object();
    assertTrue("offer() should always return true", q.offer(first));
    assertEquals("'first' should be the head of the queue", first, q.poll());
    assertTrue("queue should be empty", q.isEmpty());
    q.add(first);
    assertTrue("offer() should always return true", q.offer(second));
    assertEquals("'second' should be the head of the queue", second, q.poll());
    assertEquals("'first' should be the head of the queue", first, q.poll());
    assertTrue("queue should be empty", q.isEmpty());
    q.add(first);
    q.add(second);
    assertTrue("offer() should always return true", q.offer(third));
    assertFalse("the first object should have been removed", q.contains(first));
    assertEquals("'third' should be the head of the queue", third, q.poll());
    assertEquals("'second' should be the head of the queue", second, q.poll());
    assertTrue("queue should be empty", q.isEmpty());
  }

  @Test
  public void poll() throws Exception {
    final Object o = new Object();
    q.add(o);
    assertEquals("poll() returned the wrong object", o, q.poll());
    assertTrue("queue should be empty after polling", q.isEmpty());
  }

  @Test
  public void peek() throws Exception {
    final Object o = new Object();
    q.add(o);
    assertEquals("peek() returned the wrong object", o, q.peek());
    assertEquals("peek() should not change the size of the queue", 1, q.size());
    assertEquals("peek() should return the same object", o, q.peek());
  }

  @Test
  public void testConcurrentModification() {
    q.add(new Object());
    Iterator iterator = q.iterator();
    try {
      iterator.hasNext();
      iterator.next();
    } catch (ConcurrentModificationException unexpected) {
      throw new AssertionError("CME should not have happened", unexpected);
    }
    iterator = q.iterator();
    q.add(new Object());
    try {
      iterator.hasNext();
      fail("hasNext() should have thrown a ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // This should have happened
    }
  }

}