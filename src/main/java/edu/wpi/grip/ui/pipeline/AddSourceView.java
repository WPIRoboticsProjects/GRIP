package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.event.EventHandler;

import java.net.URL;

/**
 * A box of buttons that let the user add different kinds of {@link Source}s.  When we add new implementations, we
 * should also add a new button.
 */
public class AddSourceView extends HBox {

    private final static double ICON_SIZE_INCHES = 1.0 / 6.0;

    private final EventBus eventBus;

    public AddSourceView(EventBus eventBus) {
        this.eventBus = eventBus;

        this.setFillHeight(true);

        addButton("Add\nImage", getClass().getResource("/edu/wpi/grip/ui/icons/add-image.png"), mouseEvent -> {
            // TODO: create an ImageFileSource and send a SourceAddedEvent
            System.out.println(mouseEvent);
        });

        addButton("Add\nWebcam", getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
            // TODO: create a WebcamSource and send a SourceAddedEvent
            System.out.println(mouseEvent);
        });
    }

    /**
     * Add a new button for adding a source.  This method takes care of resizing the button graphic and setting the
     * event handler.
     */
    private void addButton(String text, URL graphicURL, EventHandler<? super MouseEvent> onMouseClicked) {
        final ImageView graphic = new ImageView(graphicURL.toString());
        graphic.setFitWidth(Screen.getPrimary().getDpi() * ICON_SIZE_INCHES);
        graphic.setFitHeight(Screen.getPrimary().getDpi() * ICON_SIZE_INCHES);

        final Button button = new Button(text, graphic);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setOnMouseClicked(onMouseClicked);

        this.getChildren().add(button);
    }
}
