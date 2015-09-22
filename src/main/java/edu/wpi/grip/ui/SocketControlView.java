package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.RangeSlider;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that renders a {@link Socket}.  This control shows a label with the socket's identifier, as well as
 * an optional control such as a slider or a number spinner that lets the user interact with the value in the socket.
 * <p>
 * This class both subscribes to and sends {@link SocketChangedEvent}s on the event bus, and acts as an intermediary
 * between EventBus events and a JavaFX property.
 */
public class SocketControlView extends AnchorPane implements Initializable {
    @FXML
    private Label identifier;

    @FXML
    private StackPane controlPane;

    private final EventBus eventBus;
    private final Socket socket;
    private final Property valueProperty;

    public SocketControlView(EventBus eventBus, Socket<?> socket) {
        checkNotNull(eventBus);
        checkNotNull(socket);

        this.eventBus = eventBus;
        this.socket = socket;
        this.valueProperty = new SimpleObjectProperty();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SocketControl.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Always set the socket to the value of valueProperty, which may be bound to a GUI property.
        this.valueProperty.addListener((observableValue, o, t1) -> this.socket.setValue(observableValue.getValue()));

        this.eventBus.register(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SocketHint<?> socketHint = this.socket.getSocketHint();
        Object[] domain = socketHint.getDomain();

        // Set the label on the control based on the identifier from the socket hint
        this.identifier.setText(socketHint.getIdentifier());

        // Set an input control of some sort based on the "view" field of the socket hint
        switch (socketHint.getView()) {
            case NONE:
                // The hint says not to show a control, so there's nothing to do here.
                break;

            case SPINNER: {
                // To use a spinner, the socket should be able to hold a number
                checkArgument(Number.class.isAssignableFrom(socketHint.getType()));

                Number initialValue = (Number) socket.getValue();

                StringBuilder stringBuilder = new StringBuilder();
                Number min = Double.MIN_VALUE, max = Double.MAX_VALUE;
                if (domain != null && domain.length == 2) {
                    // A spinner can have a min and max, if the domain holds two values.
                    min = (Number) domain[0];
                    max = (Number) domain[1];
                }

                SpinnerValueFactory spinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        min.doubleValue(), max.doubleValue(), initialValue.doubleValue());

                Spinner<Double> spinner = new Spinner<>(spinnerValueFactory);
                spinner.setEditable(true);

                this.controlPane.getChildren().add(spinner);
                this.valueProperty.bindBidirectional(spinnerValueFactory.valueProperty());

                break;
            }
            case SLIDER: {
                // To use a slider, the socket should be able to hold a number
                checkArgument(Number.class.isAssignableFrom(socketHint.getType()));

                // A slider needs a min and max, so the domain should hold two values.
                checkArgument(domain != null && domain.length == 2);
                double min = ((Number) domain[0]).doubleValue(),
                        max = ((Number) domain[1]).doubleValue(),
                        initialValue = ((Number) socket.getValue()).doubleValue();

                Slider slider = new Slider(min, max, initialValue);
                this.controlPane.getChildren().add(slider);
                this.valueProperty.bindBidirectional(slider.valueProperty());

                break;
            }
            case RANGE: {
                // A range produces an array of two numbers, so the socket should expect an array that can hold numbers
                checkArgument(socketHint.getType().isArray());
                checkArgument(Number.class.isAssignableFrom(socketHint.getType().getComponentType()));

                // A ranged slider also needs a min and max, so the domain should hold two values.
                checkArgument(domain != null && domain.length == 2);
                double min = ((Number) domain[0]).doubleValue(),
                        max = ((Number) domain[1]).doubleValue();

                // The initial value should also hold two values, for the low and high points
                Number[] initialValue = (Number[]) socket.getValue();
                checkArgument(initialValue.length == 2);
                double initialLow = initialValue[0].doubleValue(),
                        initialHigh = initialValue[1].doubleValue();

                RangeSlider rangeSlider = new RangeSlider(min, max, initialLow, initialHigh);
                this.controlPane.getChildren().add(rangeSlider);
                this.valueProperty.bindBidirectional(rangeSlider.lowValueProperty());

                break;
            }
            case SELECT:
                // TODO: implement a select control once we have some operations that have enum inputs
                break;

            default:
                // This shouldn't ever happen, but it's probably a good idea to crash instead of silently doing nothing
                // and wondering why no control is showing up.
                throw new RuntimeException("Unexpected socket view hint");
        }

        this.controlPane.disableProperty().set(this.socket.isConnected());
    }

    public Socket getSocket() {
        return this.socket;
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.socket) {
            // When the socket changes value, update valueProperty.  valueProperty may be bound to a GUI control that
            // will change appearance when the socket's value changes, such as a slider.
            this.valueProperty.setValue(this.socket.getValue());
        }
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        // Disable the control for the socket if its value is set by a connection anyway
        if (event.getConnection().getInputSocket() == this.socket) {
            this.controlPane.disableProperty().set(this.socket.isConnected());
        }
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        // Enable the control for the socket if the connection influencing its value was removed
        if (event.getConnection().getInputSocket() == this.socket) {
            this.controlPane.disableProperty().set(this.socket.isConnected());
        }
    }
}
