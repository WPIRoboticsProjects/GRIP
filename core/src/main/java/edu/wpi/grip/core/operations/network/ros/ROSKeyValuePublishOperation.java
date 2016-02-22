package edu.wpi.grip.core.operations.network.ros;


import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.KeyValuePublishOperation;
import edu.wpi.grip.core.operations.network.Publishable;

import java.util.function.Function;

/**
 *
 *
 */
public abstract class ROSKeyValuePublishOperation<S, T extends Publishable, P> extends KeyValuePublishOperation<S, T, P> {

    public ROSKeyValuePublishOperation(Manager manager) {
        super(manager);
    }

    public ROSKeyValuePublishOperation(Manager manager, Function<S, T> converter) {
        super(manager, converter);
    }

    @Override
    protected String getNetworkProtocolNameAcronym() {
        return "ROS";
    }

    @Override
    protected String getNetworkProtocolName() {
        return "Robot Operating System";
    }

    @Override
    protected String getSocketHintStringPrompt() {
        return "Topic";
    }
}
