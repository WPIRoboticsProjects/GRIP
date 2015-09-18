package edu.wpi.grip.ui.models;

import edu.wpi.grip.core.Step;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by tom on 9/14/15.
 */
public class StepModel {
    private final Step step;
    private final StringProperty operationName = new SimpleStringProperty(this, "operationName");

    public StepModel(Step step) {
        this.step = step;
    }

    public StringProperty operationNameProperty() {
        return this.operationName;
    }
}
