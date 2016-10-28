package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * An entry in the pipeline.
 */
public interface PipelineEntry {

  /**
   * Gets a read-only list of the input sockets to this entry.
   * This may be empty, but may never be null.
   */
  List<InputSocket> getInputSockets();

  /**
   * Gets a read-only list of the output sockets from this entry.
   * This may be empty, but may never be null.
   */
  List<OutputSocket> getOutputSockets();

  /**
   * Gets the socket with the given ID in this entry.
   *
   * @param uid the UID of the socket to get
   *
   * @throws NoSuchElementException if there is no socket with the given UID,
   */
  Socket getSocketByUid(String uid) throws NoSuchElementException;

  /**
   * Sets this entry as removed from the pipeline.
   */
  void setRemoved();

  /**
   * Checks if this entry has been removed from the pipeline.
   */
  boolean removed();

  /**
   * Sets the ID of this entry. This should only be used by deserialization.
   *
   * @param newId the new ID
   *
   * @throws NullPointerException     if the ID is null
   * @throws IllegalArgumentException if the ID is already taken
   */
  void setId(String newId) throws NullPointerException, IllegalArgumentException;

  /**
   * Gets the ID of this entry. This ID should be unique for all instances of the concrete class.
   */
  String getId();

}
