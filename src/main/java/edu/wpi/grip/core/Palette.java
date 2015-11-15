package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.OperationAddedEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.python.jline.internal.Preconditions.checkNotNull;

/**
 * The palette is a library of operations that can be added as steps in the {@link Pipeline}
 */
public class Palette {

    private final EventBus eventBus;
    private final Map<String, Operation> operations = new HashMap<>();

    public Palette(EventBus eventBus) {
        this.eventBus = checkNotNull(eventBus);
        eventBus.register(this);
    }

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
