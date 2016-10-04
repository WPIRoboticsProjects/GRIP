package edu.wpi.grip.core.operations.network.networktables;

import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * NetworkTables implementation for tests that doesn't rely on the networking stack.
 */
@SuppressWarnings( {"OverloadMethodsDeclarationOrder", "PMD"})
public class MockNetworkTable implements ITable {

  private final Map<String, Object> entries = new HashMap<>();
  private final Map<String, ITable> subTables = new HashMap<>();
  private final Map<String, List<ITableListener>> listeners = new HashMap<>();
  private static final List<Class> SUPPORTED_TYPES = Arrays.asList(
      byte[].class,
      String.class,
      String[].class,
      Boolean.class,
      Boolean[].class,
      boolean[].class,
      Double.class,
      Double[].class,
      double[].class
  );

  private final String name;

  private MockNetworkTable(String name) {
    this.name = name;
  }

  /**
   * Gets a table for the given path.
   *
   * Note: this will <strong>always</strong> return a new table.
   */
  public static ITable getTable(String path) {
    checkNotNull(path, "path");
    checkArgument(!path.matches(".*/{2,}.*")); // can't have multiple slashes in path
    if ("/".equals(path) || path.isEmpty()) {
      return new MockNetworkTable("/");
    }
    String absolute = path;
    if (path.charAt(0) != '/') {
      absolute = '/' + path;
    }
    String[] subPaths = absolute.split("/");
    ITable table = new MockNetworkTable("/");
    for (String p : subPaths) {
      table = table.getSubTable(p);
    }
    return table;
  }

  /**
   * Gets the type of the value for the given key, or null if there's no relevant value.
   */
  private Class getExistingType(String key) {
    if (containsKey(key)) {
      return entries.get(key).getClass();
    } else {
      return null;
    }
  }

  /**
   * Gets the relevant table for the given key e.g. "/foo/bar" -> "/foo".
   */
  private MockNetworkTable tableForKey(String key) {
    if (key.contains("/")) {
      return (MockNetworkTable) getSubTable(key.substring(0, key.lastIndexOf('/')));
    } else {
      return this;
    }
  }

  /**
   * Gets the name of the given key, e.g. "/foo/bar" -> "bar".
   */
  private String realKey(String key) {
    return key.contains("/") ? key.substring(key.lastIndexOf('/') + 1, key.length()) : key;
  }

  @Override
  public boolean containsKey(String key) {
    return entries.containsKey(key);
  }

  @Override
  public boolean containsSubTable(String key) {
    return subTables.containsKey(key);
  }

  @Override
  public ITable getSubTable(String key) {
    if (key.isEmpty()) {
      return this;
    }
    if (containsSubTable(key)) {
      return subTables.get(key);
    }
    if ("/".equals(name)) {
      String path = key;
      if (key.charAt(0) == '/') {
        path = key.substring(1);
      }
      return subTables.computeIfAbsent(path, k -> new MockNetworkTable("/" + k));
    }
    if (!key.contains("/")) {
      return subTables.computeIfAbsent(key, k -> new MockNetworkTable(name + "/" + k));
    }
    MockNetworkTable t = this;
    for (String p : key.split("/")) {
      t = (MockNetworkTable) t.getSubTable(p);
    }
    return t;
  }

  @Override
  public Set<String> getKeys(int types) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> getKeys() {
    return entries.keySet();
  }

  @Override
  public Set<String> getSubTables() {
    return subTables.keySet();
  }

  @Override
  public void setPersistent(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearPersistent(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPersistent(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFlags(String key, int flags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearFlags(String key, int flags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getFlags(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(String key) {
    tableForKey(key).entries.remove(realKey(key));
  }

  @Override
  public Object getValue(String key) throws TableKeyNotDefinedException {
    MockNetworkTable relevant = tableForKey(key);
    String name = realKey(key);
    if (relevant.containsKey(name)) {
      return relevant.entries.get(name);
    } else {
      throw new TableKeyNotDefinedException(key);
    }
  }

  @Override
  public Object getValue(String key, Object defaultValue) {
    MockNetworkTable relevant = tableForKey(key);
    String name = realKey(key);
    if (relevant.containsKey(name)) {
      return relevant.entries.getOrDefault(name, defaultValue);
    } else {
      throw new TableKeyNotDefinedException(key);
    }
  }

  @Override
  public boolean putValue(final String key, Object value) throws IllegalArgumentException {
    String path = key;
    if (key.charAt(0) == '/') {
      path = key.substring(1);
    }
    if (path.contains("/")) {
      ITable sub = tableForKey(path);
      return sub.putValue(realKey(path), value);
    }
    if (SUPPORTED_TYPES.stream().anyMatch(c -> c.isInstance(value))) {
      if (!containsKey(path) || getExistingType(path).equals(value.getClass())) {
        entries.put(path, value);
        return true;
      } else {
        return false;
      }
    } else {
      throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }
  }

  private boolean setDefaultValue(String key, Object defaultValue) {
    MockNetworkTable relevant = tableForKey(key);
    String name = realKey(key);
    if (relevant.containsKey(name) && relevant.getExistingType(name) != defaultValue.getClass()) {
      return false;
    }
    if (!relevant.containsKey(key)) {
      relevant.entries.put(key, defaultValue);
    }
    return true;
  }

  @Override
  public void retrieveValue(String key, Object externalValue) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("Deprecated");
  }

  @Override
  public boolean putNumber(String key, double value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultNumber(String key, double defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public double getNumber(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getNumber(String key, double defaultValue) {
    return (Double) getValue(key, defaultValue);
  }

  @Override
  public boolean putString(String key, String value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultString(String key, String defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public String getString(String key) throws TableKeyNotDefinedException {
    return (String) getValue(key);
  }

  @Override
  public String getString(String key, String defaultValue) {
    return (String) getValue(key, defaultValue);
  }

  @Override
  public boolean putBoolean(String key, boolean value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultBoolean(String key, boolean defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public boolean getBoolean(String key) throws TableKeyNotDefinedException {
    return (Boolean) getValue(key);
  }

  @Override
  public boolean getBoolean(String key, boolean defaultValue) {
    return (Boolean) getValue(key, defaultValue);
  }

  @Override
  public boolean putBooleanArray(String key, boolean[] value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultBooleanArray(String key, boolean[] defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public boolean putBooleanArray(String key, Boolean[] value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultBooleanArray(String key, Boolean[] defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public boolean[] getBooleanArray(String key) throws TableKeyNotDefinedException {
    return (boolean[]) getValue(key);
  }

  @Override
  public boolean[] getBooleanArray(String key, boolean[] defaultValue) {
    return (boolean[]) getValue(key, defaultValue);
  }

  @Override
  public Boolean[] getBooleanArray(String key, Boolean[] defaultValue) {
    return (Boolean[]) getValue(key, defaultValue);
  }

  @Override
  public boolean putNumberArray(String key, double[] value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultNumberArray(String key, double[] defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public boolean putNumberArray(String key, Double[] value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultNumberArray(String key, Double[] defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public double[] getNumberArray(String key) throws TableKeyNotDefinedException {
    return (double[]) getValue(key);
  }

  @Override
  public double[] getNumberArray(String key, double[] defaultValue) {
    return (double[]) getValue(key, defaultValue);
  }

  @Override
  public Double[] getNumberArray(String key, Double[] defaultValue) {
    return (Double[]) getValue(key, defaultValue);
  }

  @Override
  public boolean putStringArray(String key, String[] value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultStringArray(String key, String[] defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public String[] getStringArray(String key) throws TableKeyNotDefinedException {
    return (String[]) getValue(key);
  }

  @Override
  public String[] getStringArray(String key, String[] defaultValue) {
    return (String[]) getValue(key, defaultValue);
  }

  @Override
  public boolean putRaw(String key, byte[] value) {
    return putValue(key, value);
  }

  @Override
  public boolean setDefaultRaw(String key, byte[] defaultValue) {
    return setDefaultValue(key, defaultValue);
  }

  @Override
  public boolean putRaw(String key, ByteBuffer value, int len) {
    byte[] buf = new byte[len];
    value.get(buf, 0, len);
    return putRaw(key, buf);
  }

  @Override
  public byte[] getRaw(String key) throws TableKeyNotDefinedException {
    return (byte[]) getValue(key);
  }

  @Override
  public byte[] getRaw(String key, byte[] defaultValue) {
    return (byte[]) getValue(key, defaultValue);
  }

  @Override
  public void addTableListener(ITableListener listener) {
    listeners.computeIfAbsent("", k -> new ArrayList<>()).add(listener);
  }

  @Override
  public void addTableListener(ITableListener listener, boolean immediateNotify) {
    if (immediateNotify) {
      for (String key : getKeys()) {
        listener.valueChanged(this, key, getValue(key), false);
      }
    }
    addTableListener(listener);
  }

  @Override
  public void addTableListenerEx(ITableListener listener, int flags) {
    for (Map.Entry<String, Object> e : entries.entrySet()) {
      listener.valueChangedEx(this, e.getKey(), e.getValue(), flags);
    }
  }

  @Override
  public void addTableListener(String key, ITableListener listener, boolean immediateNotify) {
    if (immediateNotify) {
      listener.valueChanged(this, key, getValue(key), false);
    }
    listeners.computeIfAbsent(key, k -> new ArrayList<>()).add(listener);
  }

  @Override
  public void addTableListenerEx(String key, ITableListener listener, int flags) {
    listener.valueChangedEx(this, key, getValue(key), flags);
  }

  @Override
  public void addSubTableListener(ITableListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSubTableListener(ITableListener listener, boolean localNotify) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeTableListener(ITableListener listener) {
    listeners.values().forEach(l -> l.remove(listener));
  }

  @Override
  public boolean putInt(String key, int value) {
    return putNumber(key, value);
  }

  @Override
  public int getInt(String key) throws TableKeyNotDefinedException {
    return (int) getNumber(key);
  }

  @Override
  public int getInt(String key, int defaultValue) throws TableKeyNotDefinedException {
    return (int) getValue(key, defaultValue);
  }

  @Override
  public boolean putDouble(String key, double value) {
    return putNumber(key, value);
  }

  @Override
  public double getDouble(String key) throws TableKeyNotDefinedException {
    return getNumber(key);
  }

  @Override
  public double getDouble(String key, double defaultValue) {
    return getNumber(key, defaultValue);
  }

  @Override
  public String toString() {
    return "MockNetworkTable: " + name;
  }
}
