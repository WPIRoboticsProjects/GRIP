package edu.wpi.grip.core.operations.network;


import java.util.Optional;

@SuppressWarnings("PMD.UncommentedEmptyMethodBody")
public class MockNetworkPublisher<T> extends NetworkPublisher<T> {

    @Override
    public void publish(T publish) {

    }

    @Override
    protected void publishNameChanged(Optional<String> oldName, String newName) {

    }

    @Override
    public void close() {

    }
}
