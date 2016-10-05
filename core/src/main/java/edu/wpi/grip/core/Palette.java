package edu.wpi.grip.core;

import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.Operations;

import com.google.common.eventbus.EventBus;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The palette is a library of operations that can be added as steps in the {@link Pipeline}.
 */
@Singleton
public class Palette {
  private final Map<String, OperationMetaData> operations = new LinkedHashMap<>();
  private final Provider<EventBus> eventBus;

  @Inject
  protected Palette(Provider<EventBus> eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * Adds every {@link OperationMetaData} to this palette.
   * @param operations The operations to add.
   */
  public void addOperations(Operations operations) {
    operations.operations().forEach(this::addOperation);
  }

  /**
   * Adds an OperationMetaData to the palette.
   * @param operationMetaData The meta data to add.
   */
  public void addOperation(OperationMetaData operationMetaData) {
    map(operationMetaData.getDescription().name(), operationMetaData);
    for (String alias : operationMetaData.getDescription().aliases()) {
      map(alias, operationMetaData);
    }
    eventBus.get().post(new OperationAddedEvent(operationMetaData));
  }

  /**
   * Maps the key to the given operation.
   *
   * @param key       The key the operation should be mapped to
   * @param operation The operation to map the key to
   * @throws IllegalArgumentException if the key is already in the {@link #operations} map.
   */
  private void map(String key, OperationMetaData operation) {
    checkArgument(!operations.containsKey(key), "Operation name or alias already exists: " + key);
    operations.put(key, operation);
  }

  /**
   * @return A set of all available operations.
   */
  public Set<OperationMetaData> getOperations() {
    return new HashSet<>(this.operations.values());
  }

  /**
   * @return The operation with the specified unique name.
   */
  public Optional<OperationMetaData> getOperationByName(String name) {
    return Optional.ofNullable(this.operations.get(checkNotNull(name, "name cannot be null")));
  }
}
