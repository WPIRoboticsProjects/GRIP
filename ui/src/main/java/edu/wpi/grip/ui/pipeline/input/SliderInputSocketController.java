package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.util.GRIPPlatform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketController} that lets the user set the value of a number with a {@link Slider}
 */
public class SliderInputSocketController extends InputSocketController<Number> {

    private final Slider slider;
    private final Label label;
    private final GRIPPlatform platform;

    public interface Factory {
        SliderInputSocketController create(InputSocket<Number> socket);
    }

    /**
     * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s (the min and max
     *               slider values)
     */
    @Inject
    SliderInputSocketController(SocketHandleView.Factory socketHandleViewFactory, GRIPPlatform platform, @Assisted InputSocket<Number> socket) {
        super(socketHandleViewFactory, socket);
        this.platform = platform;

        final Number[] domain = socket.getSocketHint().getDomain().get();

        checkArgument(domain.length == 2,
                "Sliders must have a domain with two numbers (min and max)");

        final double min = domain[0].doubleValue();
        final double max = domain[1].doubleValue();
        final double initialValue = socket.getValue().get().doubleValue();

        this.slider = new Slider(min, max, initialValue);
        this.slider.setShowTickMarks(true);
        this.slider.setShowTickLabels(true);
        this.slider.setMajorTickUnit(max - min);
        this.slider.valueProperty().addListener(o -> this.getSocket().setValue(this.slider.getValue()));

        // Add a label under the slider to show the exact value
        this.label = new Label(String.format("%.0f", initialValue));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        this.slider.valueProperty().addListener(observable ->
                label.setText(String.format("%.0f", this.slider.getValue())));
    }

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        this.slider.disableProperty().bind(this.getHandle().connectedProperty());
        this.setContent(new VBox(this.slider, label));
    }

    @Subscribe
    public void updateSliderValue(SocketChangedEvent event) {
        if (event.isRegarding(this.getSocket())) {
            platform.runAsSoonAsPossible(()-> this.slider.setValue(this.getSocket().getValue().get().doubleValue()));
        }
    }
}
