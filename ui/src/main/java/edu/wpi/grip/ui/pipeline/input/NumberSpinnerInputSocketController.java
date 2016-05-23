package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.util.GRIPPlatform;
import edu.wpi.grip.ui.util.Spinners;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.text.NumberFormat;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketController} that lets the user set the value of a number with a {@link Spinner}
 */
public class NumberSpinnerInputSocketController extends InputSocketController<Number> {

    private final static Number[] DEFAULT_DOMAIN = new Double[]{-Double.MAX_VALUE, Double.MAX_VALUE};

    private final SpinnerValueFactory<Double> valueFactory;
    private final InvalidationListener updateSocketFromSpinner;
    private final GRIPPlatform platform;

    public interface Factory {
        NumberSpinnerInputSocketController create(InputSocket<Number> socket);
    }

    /**
     * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s (the min and max
     *               slider values), or no domain at all.
     */
    @Inject
    NumberSpinnerInputSocketController(SocketHandleView.Factory socketHandleViewFactory, GRIPPlatform platform, @Assisted InputSocket<Number> socket) {
        super(socketHandleViewFactory, socket);
        this.platform = platform;

        final Number[] domain = socket.getSocketHint().getDomain().orElse(DEFAULT_DOMAIN);

        checkArgument(domain.length == 2, "Spinners must have a domain with two numbers (min and max)");

        final double min = domain[0].doubleValue();
        final double max = domain[1].doubleValue();
        final double initialValue = socket.getValue().get().doubleValue();
        this.valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initialValue);
        this.updateSocketFromSpinner = o -> this.getSocket().setValue(this.valueFactory.getValue());
        this.valueFactory.valueProperty().addListener(this.updateSocketFromSpinner);
    }

    @FXML
    public void initialize() {
        super.initialize();
        final Spinner<Double> spinner = new Spinner<>(this.valueFactory);
        Spinners.makeEditableSafely(spinner, NumberFormat.getNumberInstance(),
                getSocket().getSocketHint().createInitialValue().get().doubleValue());
        spinner.disableProperty().bind(this.getHandle().connectedProperty());

        this.setContent(spinner);
    }

    @Subscribe
    public void updateSpinnerFromSocket(SocketChangedEvent event) {
        if (event.isRegarding(this.getSocket())) {
            platform.runAsSoonAsPossible(() -> {
                // Remove the invalidation listener when we set the value.  This listener is useful for updating the socket value
                // when the user changes the spinner, but since we're setting the spinner value from the socket value, calling it
                // here would not only be redundant, but would create an infinite loop.
                synchronized (this.valueFactory) {
                    this.valueFactory.valueProperty().removeListener(updateSocketFromSpinner);
                    this.valueFactory.setValue(this.getSocket().getValue().get().doubleValue());
                    this.valueFactory.valueProperty().addListener(updateSocketFromSpinner);
                }
            });
        }
    }

}
