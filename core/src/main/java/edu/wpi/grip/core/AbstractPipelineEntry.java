package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;

import com.google.common.base.CaseFormat;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A partial implementation of {@code PipelineEntry} that implements the removal and ID methods.
 */
public abstract class AbstractPipelineEntry implements PipelineEntry {

  private static final IdPool idPool = IdPool.INSTANCE;

  protected final Object removedLock = new Object();
  private boolean removed = false;
  private String id;

  /**
   * Creates a unique ID string for the given subclass.
   *
   * @param entryClass the subclass
   *
   * @return an ID string for the subclass.
   */
  protected static String makeId(Class<? extends PipelineEntry> entryClass) {
    String id;
    do {
      id = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entryClass.getSimpleName())
          + "." + RandomStringUtils.randomAlphanumeric(8);
    } while (idPool.checkId(entryClass, id));
    return id;
  }

  /**
   * Creates a new pipeline entry.
   *
   * @param id the ID of the new entry. This must be unique for all instances of the concrete class.
   *
   * @throws NullPointerException     if the ID is null
   * @throws IllegalArgumentException if the ID is already taken
   */
  protected AbstractPipelineEntry(String id) {
    idPool.get(getClass()).add(id);
    this.id = id;
  }

  @Override
  public final Socket getSocketByUid(String uid) throws NoSuchElementException {
    checkNotNull(uid, "UID");
    for (InputSocket in : getInputSockets()) {
      if (in.getUid().equals(uid)) {
        return in;
      }
    }
    for (OutputSocket out : getOutputSockets()) {
      if (out.getUid().equals(uid)) {
        return out;
      }
    }
    throw new NoSuchElementException(uid);
  }

  /**
   * Cleans up this entry, such as by freeing resources or disabling callbacks.
   */
  protected abstract void cleanUp();

  @Override
  public final void setRemoved() {
    synchronized (removedLock) {
      cleanUp();
      idPool.removeId(this);
      removed = true;
    }
  }

  @Override
  public final boolean removed() {
    synchronized (removedLock) {
      return removed;
    }
  }

  @Override
  public final void setId(String id) throws NullPointerException, IllegalArgumentException {
    checkNotNull(id, "The ID cannot be null");
    boolean inDeserialization = Arrays.stream(new Exception().getStackTrace())
        .map(e -> e.getClassName())
        .anyMatch(n -> n.matches(".*(Step|Source)Converter"));
    if (!inDeserialization) {
      throw new IllegalStateException(
          "This method may only be called during project deserialization");
    }
    idPool.get(getClass()).add(id);
    this.id = id;
  }

  @Override
  public final String getId() {
    return id;
  }

  @Override
  public String toString() {
    return getId();
  }

  /**
   * Pool of used IDs.
   */
  private static class IdPool extends HashMap<Class<? extends PipelineEntry>, Set<String>> {
    private static final IdPool INSTANCE = new IdPool();

    /**
     * Checks if an ID is already used by an instance of a pipeline entry class.
     */
    public boolean checkId(Class<? extends PipelineEntry> clazz, String id) {
      return get(clazz).contains(id);
    }

    /**
     * Removes the ID of the given entry.
     */
    public void removeId(PipelineEntry e) {
      get(e.getClass()).remove(e.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> get(Object key) {
      return computeIfAbsent((Class<? extends PipelineEntry>) key, k -> new HashSet<>());
    }

  }

}
