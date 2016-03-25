package edu.wpi.grip.ui.pipeline.input;


import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.javafx.scene.control.skin.ColorPickerSkin;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.util.GRIPPlatform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import org.bytedeco.javacpp.opencv_core.Scalar;

public class ColorInputSocketController extends InputSocketController<Scalar> {

    private ColorPicker colorPicker;
    private final ChangeListener<Color> updateSocketFromColor;

    public interface Factory {
        ColorInputSocketController create(InputSocket<Scalar> socket);
    }

    private static class FixedColorPicker extends ColorPicker {
        private final Parent root;
        public FixedColorPicker(Parent parent, Color color) {
            super(color);
            this.root = parent;
        }

        public FixedColorPicker(Parent parent) {
            super();
            this.root = parent;
        }

        protected Skin<?> createDefaultSkin() {
            class FixedColorPickerSkin extends ColorPickerSkin {
                private final ColorPicker colorPicker;
                public FixedColorPickerSkin(ColorPicker colorPicker) {
                    super(colorPicker);
                    this.colorPicker = colorPicker;
                }

                @Override
                public PopupControl getPopup() {
                    final PopupControl popupControl = super.getPopup();
                    colorPicker.styleProperty().bind(root.styleProperty());
                    colorPicker.getStyleClass().addAll(root.getStylesheets());
                    return popupControl;
                }
            }
            final ColorPickerSkin skin = new FixedColorPickerSkin(this);
            return skin;
        }
    }

    @Inject
    ColorInputSocketController(SocketHandleView.Factory socketHandleViewFactory, GRIPPlatform platform, @Assisted InputSocket<Scalar> socket) {
        super(socketHandleViewFactory, socket);
        this.updateSocketFromColor = (observable, o, t1) -> socket.setValue(colorToScalar(new Scalar(), observable.getValue()));

    }

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        this.colorPicker = getSocket().getValue().isPresent() ? new FixedColorPicker(getRoot(), scalarToColor(getSocket().getValue().get())) : new FixedColorPicker(getRoot());
        colorPicker.valueProperty().addListener(this.updateSocketFromColor);
        this.setContent(colorPicker);
    }

    @Subscribe
    public void updateSpinnerFromSocket(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.colorPicker.setValue(scalarToColor(this.getSocket().getValue().get()));
        }
    }

    /**
     * Converts from a Scalar to a Color
     *
     * @param scalar The scalar to convert from.
     * @return The Color equivalent to this scalar
     */
    private static Color scalarToColor(Scalar scalar) {
        final int red = (int) scalar.red();
        final int green = (int) scalar.green();
        final int blue = (int) scalar.blue();
        return Color.rgb(red, green, blue);
    }

    /**
     * Convert from a color to a Scalar
     *
     * @param modifiable The scalar to modify.
     * @param color      The color to assign to this Scalar
     * @return The modifiable scalar passed as an argument.
     */
    private static Scalar colorToScalar(Scalar modifiable, Color color) {
        return modifiable
                .red(color.getRed() * 255.0)
                .green(color.getGreen() * 255.0)
                .blue(color.getBlue() * 255.0);
    }
}
