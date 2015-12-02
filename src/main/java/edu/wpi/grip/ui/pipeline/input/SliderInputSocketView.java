package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketView} that lets the user set the value of a number with a {@link Slider}
 */
public class SliderInputSocketView extends InputSocketView<Number> {

    final Slider slider;

    /**
     * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s (the min and max
     *               slider values)
     */
    public SliderInputSocketView(EventBus eventBus, InputSocket<Number> socket) {
        super(eventBus, socket);

        final Object[] domain = socket.getSocketHint().getDomain().get();

        checkArgument(domain != null
                        && domain.length == 2
                        && domain[0] instanceof Number
                        && domain[1] instanceof Number,
                "Sliders must have a domain with two numbers (min and max)");

        final double min = ((Number) domain[0]).doubleValue();
        final double max = ((Number) domain[1]).doubleValue();
        final double initialValue = socket.getValue().get().doubleValue();

        this.slider = new Slider(min, max, initialValue);
        this.slider.setShowTickMarks(true);
        this.slider.setShowTickLabels(true);
        this.slider.setMajorTickUnit(max - min);
        this.slider.valueProperty().addListener(o -> this.getSocket().setValue(this.slider.getValue()));
        this.slider.disableProperty().bind(this.getHandle().connectedProperty());

        // Add a label under the slider to show the exact value
        final Label label = new Label(String.format("%.0f", initialValue));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        this.slider.valueProperty().addListener(observable ->
                label.setText(String.format("%.0f", this.slider.getValue())));

        this.setContent(new VBox(this.slider, label));
    }

    @Subscribe
    public void updateSliderValue(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.slider.setValue(this.getSocket().getValue().get().doubleValue());
        }
    }
}
