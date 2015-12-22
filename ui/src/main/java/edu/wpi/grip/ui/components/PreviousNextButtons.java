package edu.wpi.grip.ui.components;

import edu.wpi.grip.core.PreviousNext;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.SegmentedButton;

import java.util.function.Consumer;

/**
 * A button that can be used to control anything that is {@link PreviousNext}.
 */
public final class PreviousNextButtons extends SegmentedButton {
    protected static final String
            NEXT_BUTTON_STYLE_CLASS = "next-button",
            PREVIOUS_BUTTON_STYLE_CLASS = "previous-button";

    private static final Image
            nextImage = new Image(PreviousNextButtons.class.getResourceAsStream("/edu/wpi/grip/ui/icons/next.png")),
            previousImage = new Image(PreviousNextButtons.class.getResourceAsStream("/edu/wpi/grip/ui/icons/previous.png"));

    private final ToggleButton previousButton;
    private final ToggleButton nextButton;

    public PreviousNextButtons(PreviousNext switchable) {
        super();

        /**
         * A ToggleButton that will only say selected long enough for the consumer action to be performed.
         */
        class NonTogglingToggleButton extends ToggleButton {
            private NonTogglingToggleButton(Node graphic, String message, Consumer<PreviousNext> switchAction, String styleClass) {
                super(null, graphic);

                this.selectedProperty().addListener((observable, oldV, newV) -> {
                    // Only run when the button is selected.
                    if (!newV) return;
                    switchAction.accept(switchable);
                    // Now that the action has run we can deselect this button.
                    this.setSelected(false);
                });

                this.setTooltip(new Tooltip(message));
                this.setAccessibleText(message);
                this.getStyleClass().add(styleClass);
            }
        }

        this.previousButton = new NonTogglingToggleButton(
                createButtonGraphic(previousImage), "Previous", PreviousNext::previous, PREVIOUS_BUTTON_STYLE_CLASS);
        this.nextButton = new NonTogglingToggleButton(
                createButtonGraphic(nextImage), "Next", PreviousNext::next, NEXT_BUTTON_STYLE_CLASS);

        getButtons().addAll(previousButton, nextButton);
    }

    // Intentionally left package private for testing
    ToggleButton getPreviousButton() {
        return previousButton;
    }

    // Intentionally left package private for testing
    ToggleButton getNextButton() {
        return nextButton;
    }


    /**
     * Creates the buttons Graphic at the right resolution for the control.
     *
     * @param image The image to use as the graphic
     * @return The graphic for the button.
     */
    private Node createButtonGraphic(Image image) {
        final ImageView icon = new ImageView(image);
        icon.setFitHeight(DPIUtility.MINI_ICON_SIZE);
        icon.setFitWidth(DPIUtility.MINI_ICON_SIZE);
        return icon;
    }

}
