package edu.wpi.grip.core.operations.network;


import java.util.Optional;
import java.util.Set;

@SuppressWarnings("PMD.UncommentedEmptyMethodBody")
public class MockNetworkPublisher extends NetworkKeyValuePublisher {

    public MockNetworkPublisher(Set<String> keys) {
        super(keys);
    }

    @Override
    protected void publishNameChanged(Optional<String> oldName, String newName) {

    }

    @Override
    protected void doPublish(String key, Object value) {

    }

    @Override
    protected void doPublish(Object value) {

    }

    @Override
    protected void doPublish() {

    }

    @Override
    protected void stopPublish(String key) {

    }

    @Override
    public void close() {

    }
}
