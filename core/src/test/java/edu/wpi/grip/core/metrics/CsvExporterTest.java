package edu.wpi.grip.core.metrics;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CsvExporterTest {

  private CsvExporter exporter;

  @Test(expected = IllegalArgumentException.class)
  public void testZeroCols() {
    new CsvExporter(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCols() {
    new CsvExporter(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongHeaderNum() {
    new CsvExporter(1);
  }

  @Test
  public void addRowVarargs() {
    exporter = new CsvExporter(1, "foo");
    exporter.addRow("bar");
    assertEquals("foo", exporter.getTable().get(0, 0));
    assertEquals("bar", exporter.getTable().get(1, 0));
  }

  @Test
  public void addRowList() {
    exporter = new CsvExporter(1, "foo");
    exporter.addRow(Arrays.asList("bar"));
    assertEquals("foo", exporter.getTable().get(0, 0));
    assertEquals("bar", exporter.getTable().get(1, 0));
  }

  @Test(expected = NullPointerException.class)
  public void testNullListRow() {
    exporter = new CsvExporter(1, "abc");
    exporter.addRow((List) null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullVarargsRow() {
    exporter = new CsvExporter(1, "abc");
    exporter.addRow((Object[]) null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullDataElement() {
    exporter = new CsvExporter(2, "foo", "bar");
    exporter.addRow(null, null);
  }

  @Test
  public void clear() {
    exporter = new CsvExporter(2, "foo", "bar");
    exporter.addRow("hello", "world");
    assertEquals("hello", exporter.getTable().get(1, 0));
    assertEquals("world", exporter.getTable().get(1, 1));
    exporter.clear();
    assertNull(exporter.getTable().get(1, 0));
    assertNull(exporter.getTable().get(1, 1));
    assertTrue(exporter.getTable().row(1).isEmpty());
    assertEquals(1, exporter.getTable().rowKeySet().size());
  }

  @Test
  public void export() {
    exporter = new CsvExporter(2, "foo", "bar");
    assertEquals("foo,bar\n", exporter.export());
    exporter.addRow("hello", "world");
    assertEquals("foo,bar\nhello,world\n", exporter.export());
    exporter.clear();
    assertEquals("foo,bar\n", exporter.export());
  }

}
