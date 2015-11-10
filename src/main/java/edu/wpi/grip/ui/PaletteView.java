package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.events.OperationAddedEvent;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.lang.reflect.Method;

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

    @FXML
    private CustomTextField operationSearch;


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


        try {
            setupClearButtonField(operationSearch);
        } catch (Exception e) {
            throw new IllegalStateException("Problem calling the setupClearButtonField method on TextFields", e);
        }

        final InvalidationListener filterOperations = observable ->
                this.operations.getChildren().forEach(node -> {
                    final Operation operation = ((OperationView) node).getOperation();
                    node.setVisible(searchAlgorithmMatches(this.operationSearch.getText(), operation.getName()));
                });

        // Update the visibility of each node when either the list of nodes or the filter text changes
        this.operations.getChildren().addListener(filterOperations);
        this.operationSearch.textProperty().addListener(filterOperations);

        this.eventBus.register(this);
    }


    // XXX: This is the only way to do this unfortunately.
    // This is where this came from: https://bitbucket.org/controlsfx/controlsfx/issues/330/making-textfieldssetupclearbuttonfield
    private void setupClearButtonField(CustomTextField customTextField) throws Exception {
        Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
        m.setAccessible(true);
        m.invoke(null, customTextField, customTextField.rightProperty());
    }

    /**
     * @param searchText The text to search for
     * @param name       The name of the operation to check
     * @return true if the search text matches the name
     */
    private boolean searchAlgorithmMatches(String searchText, String name) {
        // TODO: Make this use a fuzzy search
        final String lowerCaseSearchString = searchText.toLowerCase();
        return lowerCaseSearchString.isEmpty() || name.toLowerCase().contains(lowerCaseSearchString);
    }

    /**
     * Remove all operations in the palette
     */
    public void clearOperations() {
        this.operations.getChildren().clear();
    }

    @Subscribe
    public void onOperationAdded(OperationAddedEvent event) {
        OperationView view = new OperationView(this.eventBus, event.getOperation());
        this.operations.getChildren().add(view);
    }

}
