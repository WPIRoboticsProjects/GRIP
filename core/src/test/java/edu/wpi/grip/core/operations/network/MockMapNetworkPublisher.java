package edu.wpi.grip.core.operations.network;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("PMD.UncommentedEmptyMethodBody")
public class MockMapNetworkPublisher<T> extends MapNetworkPublisher<T> implements
    MapNetworkPublisherFactory {

  public MockMapNetworkPublisher() {
    this(Collections.emptySet());
  }

  public MockMapNetworkPublisher(Set<String> keys) {
    super(keys);
  }

  @Override
  protected void doPublish() {

  }

  @Override
  protected void doPublish(Map<String, T> publishMap) {

  }

  @Override
  protected void doPublishSingle(T value) {

  }

  @Override
  protected void publishNameChanged(Optional<String> oldName, String newName) {

  }

  @Override
  public void close() {

  }

  @Override
  public <T> MapNetworkPublisher<T> create(Set<String> keys) {
    return new MockMapNetworkPublisher<>(keys);
  }
}
