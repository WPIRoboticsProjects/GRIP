package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.beans.InvalidationListener;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketView} that lets the user set the value of a number with a {@link Spinner}
 */
public class NumberSpinnerInputSocketView extends InputSocketView<Number> {

    private final static Object[] DEFAULT_DOMAIN = new Object[]{Double.MIN_VALUE, Double.MAX_VALUE};

    private final SpinnerValueFactory<Double> valueFactory;
    private final InvalidationListener updateSocketFromSpinner;

    /**
     * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s (the min and max
     *               slider values), or no domain at all.
     */
    public NumberSpinnerInputSocketView(EventBus eventBus, InputSocket<Number> socket) {
        super(eventBus, socket);


        Object[] domain = socket.getSocketHint().getDomain();
        if (domain == null) {
            domain = DEFAULT_DOMAIN;
        }

        checkArgument(domain.length == 2 && domain[0] instanceof Number && domain[1] instanceof Number,
                "Spinners must have a domain with two numbers (min and max)");

        final double min = ((Number) domain[0]).doubleValue();
        final double max = ((Number) domain[1]).doubleValue();
        final double initialValue = socket.getValue().doubleValue();

        this.valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initialValue);
        this.updateSocketFromSpinner = o -> this.getSocket().setValue(this.valueFactory.getValue());
        this.valueFactory.valueProperty().addListener(this.updateSocketFromSpinner);

        final Spinner<Double> spinner = new Spinner<>(this.valueFactory);
        spinner.setEditable(true);
        spinner.disableProperty().bind(this.getHandle().connectedProperty());
        this.setContent(spinner);
    }

    @Subscribe
    public void updateSpinnerFromSocket(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            // Remove the invalidation listener when we set the value.  This listener is useful for updating the socket value
            // when the user changes the spinner, but since we're setting the spinner value from the socket value, calling it
            // here would not only be redundant, but would create an infinite loop.
            synchronized (this.valueFactory) {
                this.valueFactory.valueProperty().removeListener(updateSocketFromSpinner);
                this.valueFactory.setValue(this.getSocket().getValue().doubleValue());
                this.valueFactory.valueProperty().addListener(updateSocketFromSpinner);
            }
        }
    }
}
