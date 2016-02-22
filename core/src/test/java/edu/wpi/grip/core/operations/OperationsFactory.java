package edu.wpi.grip.core.operations;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.operations.network.Manager;

public class OperationsFactory {

    public static Operations create(EventBus eventBus) {
        return create(eventBus, keys -> null, keys -> null);
    }

    public static Operations create(EventBus eventBus, Manager ntManager, Manager rosManager) {
        return new Operations(eventBus, ntManager, rosManager);
    }
}
