package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link SocketHint} that's type is linked between many other sockets and who's type is defined by
 * whatever {@link InputSocket} was connected to it first.
 */
public final class LinkedSocketHint extends SocketHint.SocketHintDecorator {
    /**
     * Keeps track of the sockets that control the type of this socket hint
     */
    private final Set<InputSocket> controllingSockets = new HashSet<>();
    private final Set<OutputSocket> controlledOutputSockets = new HashSet<>();
    private final EventBus eventBus;
    private Optional<Class> connectedType = Optional.empty();

    @SuppressWarnings("unchecked")
    public LinkedSocketHint(EventBus eventBus) {
        super(new Builder<>(Object.class).identifier("").build());
        this.eventBus = checkNotNull(eventBus, "EventBus cannot be null");
    }

    /**
     * Creates an {@link InputSocket} that is linked to this SocketHint
     *
     * @param hintIdentifier The identifier for this socket's SocketHint
     * @return A socket hint that's socket type is determined by this SocketHint
     */
    @SuppressWarnings("unchecked")
    public InputSocket linkedInputSocket(String hintIdentifier) {
        // Our own custom implementation of socket hint that interacts on this class when connections are
        // added and removed
        return new InputSocket(eventBus, new IdentifierOverridingSocketHintDecorator(this, hintIdentifier)) {
            @Override
            public void addConnection(Connection connection) {
                synchronized (this) {
                    controllingSockets.add(this);
                    connectedType = Optional.of(connection.getOutputSocket().getSocketHint().getType());
                }
                super.addConnection(connection);
            }

            @Override
            public void onDisconnected() {
                synchronized (this) {
                    // Remove this socket because it is no longer controlling the type of socket
                    controllingSockets.remove(this);
                    if (controllingSockets.isEmpty()) { // When the set is empty, the socket can support any type again
                        connectedType = Optional.empty();
                        // XXX: TODO: This is breaking the law of Demeter fix this
                        controlledOutputSockets.forEach(outputSocket -> {
                            final Set<Connection<?>> connections = outputSocket.getConnections();
                            connections.stream().map(ConnectionRemovedEvent::new).forEach(this.eventBus::post);
                            outputSocket.setPreviewed(false);
                            outputSocket.resetValueToInitial();
                        });
                    }
                }
                super.onDisconnected();
            }
        };
    }

    /**
     * Creates an input socket that is linked to this SocketHint.
     * This output socket will automatically be disconnected when there is no longer an input socket to guarantee the type
     * of this SocketHint
     *
     * @param hintIdentifier The identifier for this socket's SocketHint
     * @return An OutputSocket that's type is dynamically linked to this SocketHint
     */
    @SuppressWarnings("unchecked")
    public OutputSocket linkedOutputSocket(String hintIdentifier) {
        final OutputSocket outSocket = new OutputSocket(eventBus, new IdentifierOverridingSocketHintDecorator(this, hintIdentifier));
        controlledOutputSockets.add(outSocket);
        return outSocket;
    }

    @Override
    public String getTypeLabel() {
        return "<Generic>";
    }

    @Override
    public Class getType() {
        // If the type is known because one of the input sockets is connected then return that. Otherwise, return Object
        return connectedType.orElse(Object.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isCompatibleWith(SocketHint other) {
        if (connectedType.isPresent()) { // If the type is present
            // Then use this socket hint to determine if this socket can be connected
            return connectedType.get().isAssignableFrom(other.getType());
        } else {
            // Otherwise use the socket hint we are decorating to determine the supported type
            return getDecorated().isCompatibleWith(other);
        }
    }
}
