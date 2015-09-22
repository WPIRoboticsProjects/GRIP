package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that display a list of the available operations that the user may select from
 */
public class PaletteView extends VBox implements Initializable {
    private final ListProperty<Operation> operationsProperty = new SimpleListProperty<>(
            FXCollections.observableArrayList());

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

    public ListProperty<Operation> operationsProperty() {
        return this.operationsProperty;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Always keep the list of children up to date with the list of operations.  This allows operations to be
        // added during at runtime, which may be useful for features like scripting plugins or sub-pipelines.
        this.operationsProperty.addListener((observableValue, oldOperations, operations) -> {
            this.operations.getChildren().setAll(operations.stream()
                    .map(operation -> new OperationView(this.eventBus, operation))
                    .collect(Collectors.toList()));
        });
    }
}
