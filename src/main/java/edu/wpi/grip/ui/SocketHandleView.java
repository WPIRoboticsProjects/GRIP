package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The bubble next to each socket that connections go in and out of
 */
public class SocketHandleView extends Button {

    private static final PseudoClass CONNECTING_PSEUDO_CLASS = PseudoClass.getPseudoClass("connecting");
    private static final PseudoClass CONNECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("connected");

    final private SocketControlView controlView;
    final private EventBus eventBus;

    final private BooleanProperty connectingProperty = new SimpleBooleanProperty(this, "connecting", false);
    final private BooleanProperty connectedProperty = new SimpleBooleanProperty(this, "connected", false);

    public SocketHandleView(EventBus eventBus, SocketControlView controlView) {
        checkNotNull(eventBus);
        checkNotNull(controlView);

        this.controlView = controlView;
        this.eventBus = eventBus;

        this.getStyleClass().add("socket-handle");

        this.connectingProperty.addListener((observableValue, oldValue, isConnecting) ->
                this.pseudoClassStateChanged(CONNECTING_PSEUDO_CLASS, isConnecting));

        this.connectedProperty.addListener((observableValue, oldValue, isConnected) ->
                this.pseudoClassStateChanged(CONNECTED_PSEUDO_CLASS, isConnected));
    }

    /**
     * @return The boolean property that indicates if this socket is currently in the middle of being connected to
     * another socket.  In other words, the user has clicked it, but has not clicked another socket yet.
     */
    public BooleanProperty connectingProperty() {
        return this.connectingProperty;
    }

    /**
     * @return The boolean property that indicates if this socket has at least one connection going in or out of it
     */
    public BooleanProperty connectedProperty() {
        return this.connectedProperty;
    }
}
