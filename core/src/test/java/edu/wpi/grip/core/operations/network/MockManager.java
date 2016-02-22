package edu.wpi.grip.core.operations.network;


import javax.annotation.Nullable;
import java.util.Set;

public class MockManager implements Manager {

    @Override
    public NetworkKeyValuePublisher createPublisher(@Nullable Set<String> keys) {
        return null;
    }
}
