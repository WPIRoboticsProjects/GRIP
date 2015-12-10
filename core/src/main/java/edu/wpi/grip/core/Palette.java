package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.OperationAddedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The palette is a library of operations that can be added as steps in the {@link Pipeline}
 */
@Singleton
public class Palette {

    @Inject private EventBus eventBus;

    private final Map<String, Operation> operations = new LinkedHashMap<>();

    @Subscribe
    public void onOperationAdded(OperationAddedEvent event) {
        final Operation operation = event.getOperation();
        this.operations.put(operation.getName(), operation);
    }

    /**
     * @return A collection of all available operations
     */
    public Collection<Operation> getOperations() {
        return this.operations.values();
    }

    /**
     * @return The operation with the specified unique name
     */
    public Optional<Operation> getOperationByName(String name) {
        return Optional.ofNullable(this.operations.get(name));
    }
}
