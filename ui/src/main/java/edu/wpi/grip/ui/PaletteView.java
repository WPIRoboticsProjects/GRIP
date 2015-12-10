package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.ui.util.SearchUtility;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that display a list of the available operations that the user may select from
 *
 * @see OperationAddedEvent
 */
public class PaletteView extends VBox {

    @FXML
    private VBox operations;

    @FXML
    private CustomTextField operationSearch;

    private final EventBus eventBus;
    private final Palette palette;

    public PaletteView(EventBus eventBus, Palette palette) {
        checkNotNull(eventBus);

        this.eventBus = eventBus;
        this.palette = palette;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Palette.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setupClearButtonField(operationSearch);

        for (Operation operation : this.palette.getOperations()) {
            this.operations.getChildren().add(new OperationView(this.eventBus, operation));
        }


        final InvalidationListener filterOperations = observable -> {
            this.operations.getChildren().forEach(node -> {
                final Operation operation = ((OperationView) node).getOperation();
                final String searchText = this.operationSearch.getText();
                node.setVisible(SearchUtility.fuzzyContains(operation.getName(), searchText)
                        || SearchUtility.fuzzyContains(operation.getDescription(), searchText));
            });
        };

        // Update the visibility of each node when either the list of nodes or the filter text changes
        this.operations.getChildren().addListener(filterOperations);
        this.operationSearch.textProperty().addListener(filterOperations);

        // The palette should have a lower priority for resizing than other elements
        this.getProperties().put("resizable-with-parent", false);

        this.eventBus.register(this);
    }

    /**
     * Make the search box have a "clear" button
     * <p>
     * XXX: This is the only way to do this unfortunately.
     * This is where this came from: https://bitbucket.org/controlsfx/controlsfx/issues/330/making-textfieldssetupclearbuttonfield
     */
    private void setupClearButtonField(CustomTextField customTextField) {
        try {
            final Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, customTextField, customTextField.rightProperty());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove all operations in the palette
     */
    public void clearOperations() {
        this.operations.getChildren().clear();
    }

    /**
     * @return The palette of operations shown in this view
     */
    public Palette getPalette() {
        return palette;
    }

    @Subscribe
    public void onOperationAdded(OperationAddedEvent event) {
        OperationView view = new OperationView(this.eventBus, event.getOperation());
        this.operations.getChildren().add(view);
    }

}
