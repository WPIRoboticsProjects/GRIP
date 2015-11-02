package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import org.controlsfx.control.RangeSlider;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketView} that lets the user set a high and low value in a two-element list
 */
public class RangeInputSocketView extends InputSocketView<List<Number>> {

    final RangeSlider slider;

    /**
     * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s (the min and max
     *               slider values)
     */
    public RangeInputSocketView(EventBus eventBus, InputSocket<List<Number>> socket) {
        super(eventBus, socket);

        final Object[] domain = socket.getSocketHint().getDomain();
        final List<Number> value = socket.getValue();

        checkArgument(domain != null && domain.length == 1 && domain[0] instanceof List,
                "Sliders must have a domain with a list of two numbers (min and max)");

        @SuppressWarnings("unchecked")
        final List<Number> extremes = (List<Number>) domain[0];
        checkArgument(extremes.size() == 2 && extremes.get(0) instanceof Number && extremes.get(1) instanceof Number,
                "Sliders must have a domain with a list of two numbers (min and max)");

        checkArgument(value.size() == 2, "Range sliders must contain two values (low and high)");

        final double min = extremes.get(0).doubleValue();
        final double max = extremes.get(1).doubleValue();
        final double initialLow = value.get(0).doubleValue();
        final double initialHigh = value.get(1).doubleValue();

        this.slider = new RangeSlider(min, max, initialLow, initialHigh);

        // Set the socket values whenever the range changes
        this.slider.lowValueProperty().addListener(o -> {
            value.set(0, this.slider.getLowValue());

            // If the high value is also changing simultaneously, don't call setValue() twice
            if (!this.slider.isHighValueChanging()) {
                this.getSocket().setValue(value);
            }
        });

        this.slider.highValueProperty().addListener(o -> {
            value.set(1, this.slider.getHighValue());
            this.getSocket().setValue(value);
        });

        this.slider.disableProperty().bind(this.getHandle().connectedProperty());

        this.setContent(this.slider);
    }

    @Subscribe
    public void updateSliderValue(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.slider.setLowValue(this.getSocket().getValue().get(0).doubleValue());
            this.slider.setHighValue(this.getSocket().getValue().get(1).doubleValue());
        }
    }
}
