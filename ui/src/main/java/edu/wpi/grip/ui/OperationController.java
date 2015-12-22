package edu.wpi.grip.ui;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.ui.annotations.ParametrizedController;
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
    private final Operation operation;

    public interface Factory {
        OperationController create(Operation operation);
    }

    @Inject
    OperationController(Pipeline pipeline, Step.Factory stepFactory, @Assisted Operation operation) {
        this.pipeline = pipeline;
        this.stepFactory = stepFactory;
        this.operation = operation;
    }

    @FXML
    public void initialize() {
        root.setId(StyleClassNameUtility.idNameFor(this.operation));
        this.name.setText(this.operation.getName());
        this.description.setText(this.operation.getDescription());

        final Tooltip tooltip = new Tooltip(this.operation.getDescription());
        tooltip.setPrefWidth(400.0);
        tooltip.setWrapText(true);
        Tooltip.install(root, tooltip);

        this.description.setAccessibleHelp(this.operation.getDescription());

        this.operation.getIcon().ifPresent(icon -> this.icon.setImage(new Image(icon)));

        // Ensures that when this element is hidden that it also removes its size calculations
        root.managedProperty().bind(root.visibleProperty());
    }

    @FXML
    public void addStep() {
        this.pipeline.addStep(stepFactory.create(this.operation));
    }

    public GridPane getRoot() {
        return root;
    }

    public Operation getOperation() {
        return operation;
    }
}
