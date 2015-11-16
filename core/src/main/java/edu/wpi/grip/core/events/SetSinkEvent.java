package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Sink;

/**
 * The event posted when a new sink is assigned by the user.  Sinks are classes that subscribe to
 * {@link SocketPublishedEvent} and implement {@link Sink}, and typically sinks write published values to some sort of
 * network connection.  This event causes a different sink to be assigned in the pipeline and for the appropriate event
 * bus subscriptions and data fields to be updated.
 */
public class SetSinkEvent {
    private final Sink sink;

    public SetSinkEvent(Sink sink) {
        this.sink = sink;
    }

    public Sink getSink() {
        return this.sink;
    }
}
