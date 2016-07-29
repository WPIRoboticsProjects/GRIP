package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.operations.network.MapNetworkReceiverFactory;
import edu.wpi.grip.core.operations.network.MockNetworkReceiver;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockExceptionWitness;

import com.google.common.eventbus.EventBus;

import org.junit.Before;

public class NetworkTableEntrySourceTest {

  /**
   * Checks that object creation works.
   */
  @Before
  public void setup() {
    EventBus eventBus = new EventBus();
    OutputSocket.Factory osf = new MockOutputSocketFactory(eventBus);
    MapNetworkReceiverFactory nrf = new MockNetworkReceiver();

    new NetworkTableEntrySource(origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        nrf,
        "/Test/path",
        NetworkTableEntrySource.Types.BOOLEAN);
  }

}
