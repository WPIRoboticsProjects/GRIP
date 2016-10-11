package edu.wpi.grip.ui.pipeline.input;

import edu.wpi.grip.core.Range;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.pipeline.SocketHandleView;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.controlsfx.control.RangeSlider;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An {@link InputSocketController} that lets the user set a high and low value in a two-element
 * list.
 */
public class RangeInputSocketController extends InputSocketController<Range> {

  private final RangeSlider slider;

  /**
   * @param socket An <code>InputSocket</code> with a domain containing two <code>Number</code>s
   *               (the min and max slider values).
   */
  @Inject
  RangeInputSocketController(SocketHandleView.Factory socketHandleViewFactory,
                             @Assisted InputSocket<Range> socket) {
    super(socketHandleViewFactory, socket);

    final Range[] domain = socket.getSocketHint().getDomain().get();
    final Range initialValue = socket.getValue().get();

    checkArgument(domain.length == 1 && domain[0] != null,
        "Sliders must have a domain with a single range");

    final Range extremes = domain[0];

    final double min = extremes.getMin();
    final double max = extremes.getMax();
    final double initialLow = initialValue.getMin();
    final double initialHigh = initialValue.getMax();

    this.slider = new RangeSlider(min, max, initialLow, initialHigh);
    this.slider.setShowTickMarks(true);
    this.slider.setShowTickLabels(true);
    this.slider.setMajorTickUnit(max - min);

    // Set the socket values whenever the range changes
    this.slider.lowValueProperty().addListener(o -> {
      Range value = socket.getValue().get();
      value.setMin(slider.getLowValue());

      // If the high value is also changing simultaneously, don't call setValue() twice
      if (!this.slider.isHighValueChanging()) {
        socket.setValue(value);
      }
    });

    this.slider.highValueProperty().addListener(o -> {
      Range range = socket.getValue().get();
      range.setMax(slider.getHighValue());
      socket.setValue(range);
    });
  }

  @FXML
  @Override
  public void initialize() {
    super.initialize();

    // Add a label under the slider to show the exact range
    final Label label = new Label(getLowHighLabelText());
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER);
    this.slider.lowValueProperty().addListener(observable ->
        label.setText(getLowHighLabelText()));
    this.slider.highValueProperty().addListener(observable ->
        label.setText(getLowHighLabelText()));


    this.setContent(new VBox(this.slider, label));

    this.slider.disableProperty().bind(this.getHandle().connectedProperty());
  }

  private String getLowHighLabelText() {
    return String.format("%.0f - %.0f", this.slider.getLowValue(), this.slider.getHighValue());
  }

  @Subscribe
  public void updateSliderValue(SocketChangedEvent event) {
    if (event.isRegarding(this.getSocket())) {
      this.slider.setLowValue(this.getSocket().getValue().get().getMin());
      this.slider.setHighValue(this.getSocket().getValue().get().getMax());
    }
  }

  public interface Factory {
    RangeInputSocketController create(InputSocket<Range> socket);
  }
}
