package edu.wpi.grip.ui.models;

import edu.wpi.grip.core.Socket;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A JavaFX model that provides access to various pieces of data in Sockets as JavaFX
 * properties, allowing sockets to be bound to UI components
 */
public class SocketModel {
    private final Socket<?> socket;
    private final StringProperty identifier = new SimpleStringProperty(this, "identifier");
    private final StringProperty type = new SimpleStringProperty(this, "type");
    private final ObjectProperty value = new SimpleObjectProperty<>(this, "value");

    public SocketModel(Socket<?> socket) {
        this.socket = socket;
        this.setFromSocket();
    }

    /**
     * Reload the data from the socket into the properties of this object
     */
    public void setFromSocket() {
        this.identifier.set(this.socket.getSocketHint().getIdentifier());
        this.type.set(this.socket.getSocketHint().getType().getSimpleName());
        this.value.set(this.socket.getValue().toString());
    }

    /**
     * @return The underlying socket that this model represents
     */
    public Socket<?> getSocket() {
        return this.socket;
    }

    public StringProperty identifierProperty() {
        return this.identifier;
    }

    public StringProperty typeProperty() {
        return this.type;
    }

    public ObjectProperty valueProperty() {
        return this.value;
    }
}
