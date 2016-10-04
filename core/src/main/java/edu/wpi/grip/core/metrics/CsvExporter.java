package edu.wpi.grip.core.metrics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class for exporting data to CSV.
 */
public class CsvExporter {

  /**
   * The number of columns in the data table.
   */
  private final int numCols;

  /**
   * The number of rows of data in the table. This does not include the header row.
   */
  private int dataRows = 0;

  /**
   * The data table.
   */
  private final Table<Integer, Integer, Object> dt = TreeBasedTable.create();

  /**
   * Creates a new CSV exporter with the given number of data columns and column header.
   *
   * @param numCols the number of columns in the data table
   * @param headers the column headers to use
   */
  public CsvExporter(int numCols, String... headers) {
    checkArgument(numCols > 0, "There must be at least one column");
    checkNotNull(headers, "headers");
    checkArgument(numCols == headers.length,
        "Number of column headers does not match numCols");
    this.numCols = numCols;
    for (int i = 0; i < headers.length; i++) {
      dt.put(0, i, headers[i]);
    }
  }

  /**
   * Appends a data row to the bottom of the table.
   *
   * @param data the row to append
   *
   * @throws NullPointerException     if {@code data} is null, or if it contains null elements
   * @throws IllegalArgumentException if the number of elements in {@code data} is not exactly equal
   *                                  to the number of columns specified in the constructor
   * @see #addRow(Object...)
   */
  public void addRow(List<?> data) {
    addRow(data.toArray(new Object[data.size()]));
  }

  /**
   * Appends a data row to the bottom of the table.
   *
   * @param data the row to append
   *
   * @throws NullPointerException     if {@code data} is null, or if it contains null elements
   * @throws IllegalArgumentException if the number of elements in {@code data} is not exactly equal
   *                                  to the number of columns specified in the constructor
   */
  public void addRow(Object... data) {
    checkNotNull(data, "data");
    if (Arrays.stream(data).anyMatch(Objects::isNull)) {
      throw new NullPointerException("Data elements cannot be null");
    }
    checkArgument(data.length == numCols, "Wrong number of data elements");
    dataRows++;
    for (int i = 0; i < data.length; i++) {
      dt.put(dataRows, i, data[i]);
    }
  }

  /**
   * Clears the stored data in the exporter. The headers are unaffected.
   */
  public void clear() {
    for (int i = 0; i < dataRows; i++) {
      dt.row(i + 1).clear();
    }
    dataRows = 0;
  }

  /**
   * Exports a CSV table.
   *
   * @return the CSV representation of the data given to this exporter
   */
  public String export() {
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < dataRows + 1; row++) {
      for (int col = 0; col < numCols; col++) {
        sb.append(dt.get(row, col));
        if (col < numCols - 1) {
          sb.append(',');
        } else {
          sb.append('\n');
        }
      }
    }
    return sb.toString();
  }

  @VisibleForTesting
  Table<Integer, Integer, Object> getTable() {
    return dt;
  }

}
