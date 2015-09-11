package edu.wpi.grip.core.sinks;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Sink;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.events.SocketPublishedEvent;

/**
 * A sink that just prints out any values it receives.
 */
public class DummySink implements Sink {
    public DummySink() {
        System.out.println("ID\tType\tValue");
    }

    @Subscribe
    public void onSocketPublished(SocketPublishedEvent event) {
        Socket socket = event.getSocket();
        System.out.println(
                socket.getSocketHint().getIdentifier() + "\t" +
                socket.getSocketHint().getType().getSimpleName() + "\t" +
                socket.getValue());
    }
}
