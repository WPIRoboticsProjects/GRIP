package edu.wpi.grip.ui;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.*;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.dragging.OperationDragService;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * A JavaFX control that renders information about an {@link Operation}.  This is used in the palette view to present
 * the user with information on the various operations to choose from.
 */
@ParametrizedController(url = "Operation.fxml")
public class OperationController implements Controller {

    @FXML
    private GridPane root;

    @FXML
    private Label name;

    @FXML
    private Label description;

    @FXML
    private ImageView icon;

    private final Pipeline pipeline;
    private final Step.Factory stepFactory;
    private final OperationDragService operationDragService;
    private final OperationMetaData operationMetaData;

    public interface Factory {
        OperationController create(OperationMetaData operationMetaData);
    }

    @Inject
    OperationController(Pipeline pipeline,
                        Step.Factory stepFactory,
                        OperationDragService operationDragService,
                        @Assisted OperationMetaData operationMetaData) {
        this.pipeline = pipeline;
        this.stepFactory = stepFactory;
        this.operationDragService = operationDragService;
        this.operationMetaData = operationMetaData;
    }

    @FXML
    public void initialize() {
        final OperationDescription description = operationMetaData.getDescription();
        root.setId(StyleClassNameUtility.idNameFor(description));
        this.name.setText(description.name());
        this.description.setText(description.summary());

        final Tooltip tooltip = new Tooltip(description.summary());
        tooltip.setPrefWidth(400.0);
        tooltip.setWrapText(true);
        Tooltip.install(root, tooltip);

        this.description.setAccessibleHelp(description.summary());

        description.icon().ifPresent(icon -> this.icon.setImage(new Image(icon)));

        // Ensures that when this element is hidden that it also removes its size calculations
        root.managedProperty().bind(root.visibleProperty());

        root.setOnDragDetected(mouseEvent -> {
            // Tell the drag service that this is the operation that will be received
            operationDragService.beginDrag(operationMetaData, root, operationMetaData.getDescription().name());
            mouseEvent.consume();
        });

        root.setOnDragDone(mouseEvent -> {
            operationDragService.completeDrag();
        });
    }

    @FXML
    public void addStep() {
        this.pipeline.addStep(stepFactory.create(operationMetaData));
    }

    public GridPane getRoot() {
        return root;
    }

    public OperationDescription getOperationDescription() {
        return operationMetaData.getDescription();
    }
}
