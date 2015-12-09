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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Controller for a list of the available operations that the user may select from
 *
 * @see OperationAddedEvent
 */
@Singleton
public class PaletteController {

    @FXML private VBox root;
    @FXML private VBox operations;
    @FXML private CustomTextField operationSearch;
    @Inject private EventBus eventBus;
    @Inject private Palette palette;

    public void initialize() {
        // Make the search box have a "clear" button. This is the only way to do this unfortunately.
        // https://bitbucket.org/controlsfx/controlsfx/issues/330/making-textfieldssetupclearbuttonfield
        try {
            final Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, operationSearch, operationSearch.rightProperty());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (Operation operation : palette.getOperations()) {
            operations.getChildren().add(new OperationView(eventBus, operation));
        }

        final InvalidationListener filterOperations = observable -> {
            operations.getChildren().forEach(node -> {
                final Operation operation = ((OperationView) node).getOperation();
                final String searchText = operationSearch.getText();
                node.setVisible(SearchUtility.fuzzyContains(operation.getName(), searchText)
                        || SearchUtility.fuzzyContains(operation.getDescription(), searchText));
            });
        };

        // Update the visibility of each node when either the list of nodes or the filter text changes
        operations.getChildren().addListener(filterOperations);
        operationSearch.textProperty().addListener(filterOperations);

        // The palette should have a lower priority for resizing than other elements
        root.getProperties().put("resizable-with-parent", false);
    }

    /**
     * Remove all operations in the palette.  Used for tests.
     */
    public void clearOperations() {
        operations.getChildren().clear();
    }

    @Subscribe
    public void onOperationAdded(OperationAddedEvent event) {
        OperationView view = new OperationView(eventBus, event.getOperation());
        operations.getChildren().add(view);
    }
}
