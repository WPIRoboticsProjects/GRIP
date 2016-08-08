package edu.wpi.grip.core.operations.network;


import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MapNetworkPublisherTest {
  private static final String SHOULD_NOT_HAVE_BEEN_CALLED = "Should not have been called";

  @Test
  public void testPublisherCallsCorrectDoPublishWhenKeysAreProvided() {
    boolean[] doPublishMapWasCalled = {false};
    final DoubleMapPublisher doubleMapPublisher =
        new DoubleMapPublisher(new HashSet<>(Arrays.asList("Apple"))) {

          @Override
          protected void doPublish() {
            fail(SHOULD_NOT_HAVE_BEEN_CALLED);
          }

          @Override
          protected void doPublish(Map<String, Double> publishMap) {
            assertTrue("Map should be empty", publishMap.isEmpty());
            doPublishMapWasCalled[0] = true;
          }

          @Override
          protected void doPublishSingle(Double value) {
            fail(SHOULD_NOT_HAVE_BEEN_CALLED);
          }
        };
    doubleMapPublisher.setName("Don't care");
    doubleMapPublisher.publish(new HashMap<>());
    assertTrue("doPublish should have been called", doPublishMapWasCalled[0]);
  }

  @Test
  public void testPublisherCallsCorrectDoPublishWhenNoKeysAreProvided() {
    boolean[] doPublishNothingWasCalled = {false};
    final DoubleMapPublisher doubleMapPublisher =
        new DoubleMapPublisher(new HashSet<>()) {

          @Override
          protected void doPublish() {
            doPublishNothingWasCalled[0] = true;
          }

          @Override
          protected void doPublish(Map<String, Double> publishMap) {
            fail(SHOULD_NOT_HAVE_BEEN_CALLED);
          }

          @Override
          protected void doPublishSingle(Double value) {
            fail(SHOULD_NOT_HAVE_BEEN_CALLED);
          }
        };
    doubleMapPublisher.setName("Don't care");
    doubleMapPublisher.publish(new HashMap<>());
    assertTrue("doPublish should have been called", doPublishNothingWasCalled[0]);
  }

  @Test
  public void testPublisherCallsCorrectDoPublishWhenNoKeysAreProvidedAndMapIsNotEmpty() {
    final double EXPECTED_VALUE = Math.PI;
    boolean[] doPublishSingleValueWasCalled = {false};
    final DoubleMapPublisher doubleMapPublisher =
        new DoubleMapPublisher(new HashSet<>()) {

          @Override
          protected void doPublish() {
            fail(SHOULD_NOT_HAVE_BEEN_CALLED);
          }

          @Override
          protected void doPublish(Map<String, Double> publishMap) {
            fail(SHOULD_NOT_HAVE_BEEN_CALLED);
          }

          @Override
          protected void doPublishSingle(Double value) {
            doPublishSingleValueWasCalled[0] = true;
            assertEquals("Should have published the expected value", EXPECTED_VALUE, value, 0.001);
          }
        };
    doubleMapPublisher.setName("Don't care");
    doubleMapPublisher.publish(ImmutableMap.of("", EXPECTED_VALUE));
    assertTrue("doPublish with a single value should have been called",
        doPublishSingleValueWasCalled[0]);
  }

  private abstract static class DoubleMapPublisher extends MapNetworkPublisher<Double> {

    protected DoubleMapPublisher(Set<String> keys) {
      super(keys);
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    protected void publishNameChanged(Optional<String> oldName, String newName) {
      /* no-op */
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void close() {
      /* no-op */
    }
  }

}
