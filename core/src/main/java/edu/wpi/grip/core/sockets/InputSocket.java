package edu.wpi.grip.core.sockets;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;

import java.util.Optional;
import java.util.Set;

/**
 * Represents the input into an {@link Operation}.
 *
 * @param <T> The type of the value that this socket stores
 */
public interface InputSocket<T> extends Socket<T> {

    interface Factory {
        <T> InputSocket<T> create(SocketHint<T> hint);
    }


    /**
     * Checks if the socket has been dirtied and rests it to false.
     *
     * @return True if the socket has been dirtied
     */
    boolean dirtied();

    /**
     * Should be only called by parent classes.
     */
    void onValueChanged();


    /**
     * A decorator for the {@link InputSocket}
     *
     * @param <T> The type of the value that this socket stores
     */
    abstract class Decorator<T> implements InputSocket<T> {

        private final InputSocket<T> decorated;

        /**
         * @param socket the decorated socket
         */
        public Decorator(InputSocket<T> socket) {
            this.decorated = socket;
        }

        @Override
        public Direction getDirection() {
            return decorated.getDirection();
        }

        @Override
        public Optional<Source> getSource() {
            return decorated.getSource();
        }

        @Override
        public Optional<Step> getStep() {
            return decorated.getStep();
        }

        @Override
        public Optional<T> getValue() {
            return decorated.getValue();
        }

        @Override
        public void addConnection(Connection connection) {
            decorated.addConnection(connection);
        }

        @Override
        public void removeConnection(Connection connection) {
            decorated.removeConnection(connection);
        }

        @Override
        public Set<Connection> getConnections() {
            return decorated.getConnections();
        }

        @Override
        public SocketHint<T> getSocketHint() {
            return decorated.getSocketHint();
        }

        @Override
        public void setSource(Optional<Source> source) {
            decorated.setSource(source);
        }

        @Override
        public void setStep(Optional<Step> step) {
            decorated.setStep(step);
        }

        @Override
        public void setValueOptional(Optional<? extends T> optionalValue) {
            decorated.setValueOptional(optionalValue);
        }

        @Override
        public boolean dirtied() {
            return decorated.dirtied();
        }

        @Override
        public void onValueChanged() {
            decorated.onValueChanged();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof InputSocket && decorated.equals(o);
        }

        @Override
        public int hashCode() {
            return decorated.hashCode();
        }
    }

}
