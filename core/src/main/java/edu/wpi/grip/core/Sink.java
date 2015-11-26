package edu.wpi.grip.core;

/**
 * Anything that can serve as an output to send socketto should implement the <code>Sink</code> interface.  Typically,
 * sinks subscribe to {@link edu.wpi.grip.core.events.SocketPublishedEvent} and write socket values to some network
 * protocol.
 */
public interface Sink {
}
