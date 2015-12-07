package edu.wpi.grip.ui.pipeline.source;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.SwitchableSource;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides controls for a {@link SwitchableSource}
 */
public class SwitchableSourceControlsView extends SourceControlsView<SwitchableSource> {

    private static final Image nextImage = new Image(StopStartSourceControlsView.class.getResourceAsStream("/edu/wpi/grip/ui/icons/next.png"));
    private static final Image previousImage = new Image(StopStartSourceControlsView.class.getResourceAsStream("/edu/wpi/grip/ui/icons/previous.png"));

    private final ExceptionWitness witness;

    public SwitchableSourceControlsView(final EventBus eventBus, final SwitchableSource source) {
        checkNotNull(source, "Source can not be null");
        this.witness = new ExceptionWitness(eventBus, source);

        final ToggleButton nextButton = new ToggleButton(null, createButtonGraphic(nextImage));
        nextButton.selectedProperty().addListener(event -> {
            try {
                source.nextValue();
                witness.clearException();
            } catch (IOException e) {
                e.printStackTrace();
                witness.flagException(e);
            }
            nextButton.setSelected(false);
        });
        final String nextMessage = "Next";
        nextButton.setTooltip(new Tooltip(nextMessage));
        nextButton.setAccessibleText(nextMessage);

        final ToggleButton previousButton = new ToggleButton(null, createButtonGraphic(previousImage));
        previousButton.selectedProperty().addListener(event -> {
            try {
                source.previousValue();
                witness.clearException();
            } catch (IOException e) {
                e.printStackTrace();
                witness.flagException(e);
            }
            previousButton.setSelected(false);
        });
        final String previousMessage = "Previous";
        previousButton.setTooltip(new Tooltip(previousMessage));
        previousButton.setAccessibleText(previousMessage);

        this.getChildren().addAll(new SegmentedButton(previousButton, nextButton));
    }

    private ImageView createButtonGraphic(Image image) {
        final ImageView icon = new ImageView(image);
        icon.setFitHeight(DPIUtility.MINI_ICON_SIZE);
        icon.setFitWidth(DPIUtility.MINI_ICON_SIZE);
        return icon;
    }
}
