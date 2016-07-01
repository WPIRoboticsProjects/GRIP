package edu.wpi.grip.core.composite;


import edu.wpi.grip.core.operations.composite.SaveImageOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockFileManager;

import com.google.common.eventbus.EventBus;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SaveImageOperationTest {

  private InputSocket<Boolean> activeSocket;

  @Before
  public void setUp() throws Exception {
    EventBus eventBus = new EventBus();
    SaveImageOperation operation = new SaveImageOperation(new MockInputSocketFactory(eventBus),
        new MockOutputSocketFactory(eventBus), new MockFileManager());
    activeSocket = operation.getInputSockets().stream().filter(
        o -> o.getSocketHint().getIdentifier().equals("Active")
            && o.getSocketHint().getType().equals(Boolean.class)).findFirst().get();
  }

  @Test
  public void testActiveButtonDefaultsDisabled() {
    assertFalse("The active socket was not false (disabled).", activeSocket.getValue().get());
  }
}
