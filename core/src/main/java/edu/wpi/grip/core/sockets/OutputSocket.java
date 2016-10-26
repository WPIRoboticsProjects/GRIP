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
   *
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

  interface Factory {
    /**
     * Creates a new output socket from a socket hint. This should <i>only</i> be used for
     * generated sockets (like for Python operations) or for templated operations. For <i>
     * everything else</i>, use {@link #create(SocketHint, String)}.
     */
    default <T> OutputSocket<T> create(SocketHint<T> hint) {
      return create(hint, hint.getIdentifier().toLowerCase().replaceAll("\\s+", "-"));
    }

    <T> OutputSocket<T> create(SocketHint<T> hint, String uid);
  }
}
