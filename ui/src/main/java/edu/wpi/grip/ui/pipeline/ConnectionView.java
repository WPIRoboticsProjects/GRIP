package edu.wpi.grip.ui.pipeline;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.ui.util.DPIUtility;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX shape that renders itself as a curve connecting two sockets.  This is used to show when outputs are
 * connected to which inputs between different steps in the pipeline.a
 */
public class ConnectionView extends CubicCurve {

    private final Connection connection;
    private final ObjectProperty<Point2D> outputHandle = new SimpleObjectProperty<>(this, "outputHandle", new Point2D(0.0, 0.0));
    private final ObjectProperty<Point2D> inputHandle = new SimpleObjectProperty<>(this, "inputHandle", new Point2D(0.0, 0.0));

    public ConnectionView(Connection connection) {
        this.connection = checkNotNull(connection, "The connection can not be null");

        this.setStrokeWidth(DPIUtility.STROKE_WIDTH);
        this.getStyleClass().addAll("connection", StyleClassNameUtility.classNameFor(connection));

        this.controlX1Property().bind(this.startXProperty().add(this.endXProperty()).multiply(0.5));
        this.controlY1Property().bind(this.startYProperty());
        this.controlX2Property().bind(this.controlX1Property());
        this.controlY2Property().bind(this.endYProperty());

        this.outputHandleProperty().addListener(observable -> {
            this.setStartX(this.outputHandle.get().getX());
            this.setStartY(this.outputHandle.get().getY());
        });

        this.inputHandleProperty().addListener(observable -> {
            this.setEndX(this.inputHandle.get().getX());
            this.setEndY(this.inputHandle.get().getY());
        });
    }

    public ObjectProperty<Point2D> outputHandleProperty() {
        return this.outputHandle;
    }

    public ObjectProperty<Point2D> inputHandleProperty() {
        return this.inputHandle;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
