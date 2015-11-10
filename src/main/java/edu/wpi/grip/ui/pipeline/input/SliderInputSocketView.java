package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.scene.control.Slider;

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

        final Object[] domain = socket.getSocketHint().getDomain();

        checkArgument(domain != null
                        && domain.length == 2
                        && domain[0] instanceof Number
                        && domain[1] instanceof Number,
                "Sliders must have a domain with two numbers (min and max)");

        final double min = ((Number) domain[0]).doubleValue();
        final double max = ((Number) domain[1]).doubleValue();
        final double initialValue = socket.getValue().doubleValue();

        this.slider = new Slider(min, max, initialValue);
        this.slider.setShowTickMarks(true);
        this.slider.setShowTickLabels(true);
        this.slider.valueProperty().addListener(o -> this.getSocket().setValue(this.slider.getValue()));
        this.slider.disableProperty().bind(this.getHandle().connectedProperty());

        this.setContent(this.slider);
    }

    @Subscribe
    public void updateSliderValue(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.slider.setValue(this.getSocket().getValue().doubleValue());
        }
    }
}
