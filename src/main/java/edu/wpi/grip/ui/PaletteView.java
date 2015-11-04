package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.OperationAddedEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that display a list of the available operations that the user may select from
 *
 * @see OperationAddedEvent
 */
public class PaletteView extends VBox {

    private final EventBus eventBus;

    @FXML
    private VBox operations;

    public PaletteView(EventBus eventBus) {
        checkNotNull(eventBus);

        this.eventBus = eventBus;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Palette.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.eventBus.register(this);
    }

    /**
     * Remove all operations in the palette
     */
    public void clearOperations() {
        this.operations.getChildren().clear();
    }

    @Subscribe
    public void onOperationAdded(OperationAddedEvent event) {
        this.operations.getChildren().add(new OperationView(this.eventBus, event.getOperation()));
    }
}
