package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Connection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX shape that renders itself as a curve connecting two sockets.  This is used to show when outputs are
 * connected to which inputs between different steps in the pipeline.a
 */
public class ConnectionView extends CubicCurve {

    static final Paint FILL = null;
    static final Paint STROKE = Paint.valueOf("#575757");

    private final EventBus eventBus;
    private final Connection connection;
    private final ObjectProperty<Point2D> outputHandle = new SimpleObjectProperty<>(this, "outputHandle", new Point2D(0.0, 0.0));
    private final ObjectProperty<Point2D> inputHandle = new SimpleObjectProperty<>(this, "inputHandle", new Point2D(0.0, 0.0));

    public ConnectionView(EventBus eventBus, Connection connection) {
        checkNotNull(eventBus);

        this.eventBus = eventBus;
        this.connection = connection;

        this.setFill(FILL);
        this.setStroke(STROKE);
        this.setStrokeWidth(2.0);

        this.outputHandleProperty().addListener(observable -> {
            this.setStartX(this.outputHandle.get().getX());
            this.setStartY(this.outputHandle.get().getY());
            this.setControlY1(this.outputHandle.get().getY());
            this.setControlX2(this.outputHandle.get().getX());
        });

        this.inputHandleProperty().addListener(observable -> {
            this.setEndX(this.inputHandle.get().getX());
            this.setEndY(this.inputHandle.get().getY());
            this.setControlY2(this.inputHandle.get().getY());
            this.setControlX1(this.inputHandle.get().getX());
        });

        this.eventBus.register(this);
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
