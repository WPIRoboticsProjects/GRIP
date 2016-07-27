package edu.wpi.grip.core;

import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.OperationRemovedEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The palette is a library of operations that can be added as steps in the {@link Pipeline}.
 */
@Singleton
public class Palette {

  @Inject private EventBus eventBus;
  private final Map<String, OperationMetaData> operations = new LinkedHashMap<>();

  @Subscribe
  public void onOperationAdded(OperationAddedEvent event) {
    final OperationMetaData operation = event.getOperation();
    map(operation.getDescription().name(), operation);
    for (String alias : operation.getDescription().aliases()) {
      map(alias, operation);
    }
  }

  /**
   * Maps the key to the given operation.
   *
   * @param key       The key the operation should be mapped to
   * @param operation The operation to map the key to
   * @throws IllegalArgumentException if the key is already in the {@link #operations} map.
   */
  private void map(String key, OperationMetaData operation) {
    if (operations.containsKey(key)) {
      OperationDescription existing = operations.get(key).getDescription();
      if (existing.category() == operation.getDescription().category()
          && existing.category() == OperationDescription.Category.CUSTOM) {
        // It's a custom operation that can be changed at runtime, allow it
        // (But first remove the existing operation)
        operations.remove(key);
        eventBus.post(new OperationRemovedEvent(existing));
      } else {
        // Not a custom operation, this should only happen if someone
        // adds a new operation and uses an already-taken name
        throw new IllegalArgumentException("Operation name or alias already exists: " + key);
      }
    }
    operations.put(key, operation);
  }

  /**
   * @return A collection of all available operations.
   */
  public Collection<OperationMetaData> getOperations() {
    return this.operations.values();
  }

  /**
   * @return The operation with the specified unique name.
   */
  public Optional<OperationMetaData> getOperationByName(String name) {
    return Optional.ofNullable(this.operations.get(checkNotNull(name, "name cannot be null")));
  }
}
