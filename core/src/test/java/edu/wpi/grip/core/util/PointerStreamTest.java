package edu.wpi.grip.core.util;

import org.bytedeco.javacpp.opencv_core.MatVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointerStreamTest {

  private MatVector vector;

  @Before
  public void setupVector() {
    vector = new MatVector();
  }

  @After
  public void freeVector() {
    vector.deallocate();
  }

  @Test
  public void testStreamEmptyMatVector() {
    vector.resize(0);
    long size = PointerStream.ofMatVector(vector).count();
    assertEquals("MatVector of size 0 should result in an empty stream", 0, size);
  }

  @Test
  public void testStreamMatVectorWithContents() {
    final int size = 4;
    vector.resize(size);
    long actual = PointerStream.ofMatVector(vector).count();
    assertEquals("MatVector of size 4 should have 4 stream elements", size, actual);
  }
}
