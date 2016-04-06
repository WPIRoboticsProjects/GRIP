package edu.wpi.grip.ui;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.util.ControllerMap;
import edu.wpi.grip.ui.util.SearchUtility;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

/**
 * Controller for a VBox of {@link OperationController}s.  The user data for this should be what category it shows,
 * and the filterText property allows for fuzzy searching the operations.
 *
 * @see edu.wpi.grip.core.Operation.Category
 */
@ParametrizedController(url = "OperationList.fxml")
public class OperationListController {

    protected final static String FILTER_TEXT = "filterText";

    @FXML private Tab root;
    @FXML private VBox operations;

    @Inject private OperationController.Factory operationControllerFactory;

    private final StringProperty filterText = new SimpleStringProperty(this, FILTER_TEXT, "");
    private String baseText = null;
    private ControllerMap<OperationController, Node> operationsMapManager;

    @FXML
    public void initialize() {
        operationsMapManager = new ControllerMap<>(operations.getChildren());
        baseText = root.getText();

        InvalidationListener filterOperations = observable -> {
            if (baseText == null) {
                baseText = root.getText();
            }

            // Show only operations matching the current filter text
            String filter = filterText.getValue();
            long numMatches = operationsMapManager.keySet().stream()
                    .filter(key -> {
                        boolean visible = SearchUtility.fuzzyContains(key.getOperation().getName(), filter)
                                || SearchUtility.fuzzyContains(key.getOperation().getDescription(), filter);
                        operationsMapManager.get(key).setVisible(visible);
                        return visible;
                    }).count();

            if (!filter.isEmpty() && numMatches > 0) {
                // If we're filtering some operations and there's at least one match, set the title to bold and show the
                // number of matches.  This lets the user quickly see which tabs have matching operations when
                // searching.
                root.setText(baseText + " (" + numMatches + ")");
                root.styleProperty().setValue("-fx-font-weight: bold");
            } else {
                root.setText(baseText);
                root.styleProperty().setValue("");
            }
        };

        operations.getChildren().addListener(filterOperations);
        filterText.addListener(filterOperations);

        root.getProperties().addListener((MapChangeListener<? super Object, ? super Object>) change -> {
            if (change.getKey().equals(filterText.getName())) {
                filterText.setValue((String) root.getProperties().get(filterText.getName()));
            }
        });
    }

    @Subscribe
    public void onOperationAdded(OperationAddedEvent event) {
        Operation operation = event.getOperation();

        if (root.getUserData() == null || operation.getCategory() == root.getUserData()) {
            PlatformImpl.runAndWait(() ->
                    operationsMapManager.add(operationControllerFactory.create(event.getOperation())));
        }
    }

    /**
     * Remove all operations.  Used for tests.
     */
    @VisibleForTesting
    void clearOperations() {
        operationsMapManager.clear();
    }
}
