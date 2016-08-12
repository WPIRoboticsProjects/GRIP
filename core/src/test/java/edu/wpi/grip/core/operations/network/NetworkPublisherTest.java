package edu.wpi.grip.core.operations.network;


import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class NetworkPublisherTest {
  @Test
  public void testThatNameChangeIsCalledInitiallyButNotAgainIfNameIsSame() {
    final String NAME = "QUACKERY!";
    final int[] publishNameChangedCallCount = {0};
    final TestNetworkPublisher publisher = new TestNetworkPublisher() {
      @Override
      protected void publishNameChanged(Optional<String> oldName, String newName) {
        publishNameChangedCallCount[0]++;
        assertEquals("Name was not the new name", NAME, newName);
      }
    };
    publisher.setName(NAME);
    assertEquals("publishNameChanged was called an unexpected number of times", 1,
        publishNameChangedCallCount[0]);
    publisher.setName(NAME);
    assertEquals("publishNameChanged should not have been called agian", 1,
        publishNameChangedCallCount[0]);
  }

  private abstract static class TestNetworkPublisher extends NetworkPublisher<Double> {

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void publish(Double publish) {
      /* no-op */
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void close() {
      /* no-op */
    }
  }
}
