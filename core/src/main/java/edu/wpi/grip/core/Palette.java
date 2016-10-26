package edu.wpi.grip.core;

import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.sockets.Socket;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The palette is a library of operations that can be added as steps in the {@link Pipeline}.
 */
@Singleton
public class Palette {

  private final Map<String, OperationMetaData> operations = new LinkedHashMap<>();

  @Subscribe
  public void onOperationAdded(OperationAddedEvent event) {
    final OperationMetaData operationData = event.getOperation();
    map(operationData.getDescription().name(), operationData);
    for (String alias : operationData.getDescription().aliases()) {
      map(alias, operationData);
    }
    // Validate that every input and output socket has a unique name and UID
    Operation operation = operationData.getOperationSupplier().get();
    try {
      final List<? extends Socket> sockets = new ImmutableList.Builder<Socket>()
          .addAll(operation.getInputSockets())
          .addAll(operation.getOutputSockets())
          .build();
      checkDuplicates(
          operationData,
          "input socket names",
          operation.getInputSockets(), s -> s.getSocketHint().getIdentifier()
      );
      checkDuplicates(
          operationData,
          "output socket names",
          operation.getOutputSockets(), s -> s.getSocketHint().getIdentifier()
      );
      checkDuplicates(operationData, "socket IDs", sockets, Socket::getUid);
    } finally {
      operation.cleanUp();
    }
  }

  private static <T, U> void checkDuplicates(OperationMetaData operationMetaData,
                                             String type,
                                             List<T> list,
                                             Function<T, U> extractionFunction) {
    List<U> duplicates = list.stream()
        .map(extractionFunction)
        .collect(Collectors.toList());
    list.stream()
        .map(extractionFunction)
        .distinct()
        .forEach(duplicates::remove);
    if (!duplicates.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Duplicate %s found in operation %s: %s",
              type,
              operationMetaData.getDescription().name(),
              duplicates
          )
      );
    }
  }

  /**
   * Maps the key to the given operation.
   *
   * @param key       The key the operation should be mapped to
   * @param operation The operation to map the key to
   *
   * @throws IllegalArgumentException if the key is already in the {@link #operations} map.
   */
  private void map(String key, OperationMetaData operation) {
    checkArgument(!operations.containsKey(key), "Operation name or alias already exists: " + key);
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
