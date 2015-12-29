package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.controlsfx.control.RangeSlider;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketView} that lets the user set a high and low value in a two-element list
 */
public class RangeInputSocketView extends InputSocketView<List<Number>> {

    private final RangeSlider slider;

    /**
     * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s (the min and max
     *               slider values)
     */
    public RangeInputSocketView(EventBus eventBus, InputSocket<List<Number>> socket) {
        super(eventBus, socket);

        final Object[] domain = socket.getSocketHint().getDomain().get();
        final List<Number> initialValue = socket.getValue().get();

        checkArgument(domain.length == 1 && domain[0] instanceof List,
                "Sliders must have a domain with a list of two numbers (min and max)");

        @SuppressWarnings("unchecked")
        final List<Number> extremes = (List<Number>) domain[0];
        checkArgument(extremes.size() == 2 && extremes.get(0) instanceof Number && extremes.get(1) instanceof Number,
                "Sliders must have a domain with a list of two numbers (min and max)");

        checkArgument(initialValue.size() == 2, "Range sliders must contain two values (low and high)");

        final double min = extremes.get(0).doubleValue();
        final double max = extremes.get(1).doubleValue();
        final double initialLow = initialValue.get(0).doubleValue();
        final double initialHigh = initialValue.get(1).doubleValue();

        this.slider = new RangeSlider(min, max, initialLow, initialHigh);
        this.slider.setShowTickMarks(true);
        this.slider.setShowTickLabels(true);
        this.slider.setMajorTickUnit(max - min);

        // Set the socket values whenever the range changes
        this.slider.lowValueProperty().addListener(o -> {
            // If the high value is also changing simultaneously, don't call setValue() twice
            if (!this.slider.isHighValueChanging()) {
                List<Number> value = socket.getValue().get();
                value.set(0, slider.getLowValue());
                socket.setValue(value);
            }
        });

        this.slider.highValueProperty().addListener(o -> {
            List<Number> range = socket.getValue().get();
            range.set(1, slider.getHighValue());
            socket.setValue(range);
        });

        this.slider.disableProperty().bind(this.getHandle().connectedProperty());

        // Add a label under the slider to show the exact range
        final Label label = new Label(String.format("%.0f - %.0f", initialLow, initialHigh));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        this.slider.lowValueProperty().addListener(observable ->
                label.setText(String.format("%.0f - %.0f", this.slider.getLowValue(), this.slider.getHighValue())));
        this.slider.highValueProperty().addListener(observable ->
                label.setText(String.format("%.0f - %.0f", this.slider.getLowValue(), this.slider.getHighValue())));


        this.setContent(new VBox(this.slider, label));
    }

    @Subscribe
    public void updateSliderValue(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.slider.setLowValue(this.getSocket().getValue().get().get(0).doubleValue());
            this.slider.setHighValue(this.getSocket().getValue().get().get(1).doubleValue());
        }
    }
}
