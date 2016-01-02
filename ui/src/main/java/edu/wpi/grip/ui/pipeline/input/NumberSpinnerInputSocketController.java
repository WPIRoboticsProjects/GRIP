package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.util.GRIPPlatform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

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
        spinner.setEditable(true);
        spinner.disableProperty().bind(this.getHandle().connectedProperty());
        spinner.focusedProperty().addListener((s, ov, nv) -> {// Code found at http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
            if (nv) return;
            commitEditorText(spinner);
        });
        this.setContent(spinner);
    }

    @Subscribe
    public void updateSpinnerFromSocket(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
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

    // Code found at http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }
}
