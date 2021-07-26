package edu.wpi.grip.core.http;

import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps a record of contexts claimed by HTTP request handlers.
 */
@Singleton
public class ContextStore {

  private final Set<String> store = new HashSet<>();

  /**
   * Records the given context.
   *
   * @param context the context to record. This cannot be null.
   * @throws IllegalArgumentException if the given context has already been claimed
   */
  public void record(@Nonnull String context) throws IllegalArgumentException {
    checkNotNull(context);
    if (!store.add(context)) {
      throw new IllegalArgumentException("Context is already claimed: " + context);
    }
  }

  /**
   * Erases the given context from this store, if it's present. If {@code context} is {@code null},
   * this will do nothing and return {@code false}.
   *
   * @param context the context to erase
   * @return true if the context was erased, false if it wasn't erased
   *         or if it wasn't present to begin with.
   */
  public boolean erase(@Nullable String context) {
    return store.remove(context);
  }

  /**
   * Checks if the given context has been recorded in this store.
   * If {@code context} is {@code null}, this will return {@code false}.
   *
   * @param context the context to check
   * @return true if the given context has been recorded in this store
   */
  public boolean contains(@Nullable String context) {
    return store.contains(context);
  }


}
