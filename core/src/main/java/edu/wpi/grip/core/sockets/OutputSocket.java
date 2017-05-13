package edu.wpi.grip.core.sockets;


import edu.wpi.grip.core.Operation;

/**
 * Represents the output of an {@link Operation}.
 *
 * @param <T> The type of the value that this socket stores.
 */
public interface OutputSocket<T> extends Socket<T> {

  /**
   * @return Whether or not this socket is shown in a preview in the GUI.
   * @see #setPreviewed(boolean) d(boolean)
   */
  boolean isPreviewed();

  /**
   * @param previewed If <code>true</code>, this socket will be shown in a preview in the GUI.
   */
  void setPreviewed(boolean previewed);

  /**
   * Resets the value of this socket to its initial value.
   */
  void resetValueToInitial();

  /**
   * Notifies this socket that the value changed. This is usually only needed for sockets that
   * contain mutable data such as images or other native classes (Point, Size, etc) that are
   * written to by OpenCV operations.
   */
  default void flagChanged() {
    setValueOptional(getValue());
  }

  interface Factory {
    <T> OutputSocket<T> create(SocketHint<T> hint);
  }
}
