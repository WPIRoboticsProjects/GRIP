package edu.wpi.grip.core.operations.networktables;

import java.util.function.Function;

/**
 * An adapter to allow numbers to be published from GRIP sockets into NetworkTables
 *
 * @see NTPublishOperation#NTPublishOperation(Class, Class, Function)
 */
public class NTNumber implements NTPublishable {

    private final double number;

    public NTNumber(Number number) {
        this.number = number.doubleValue();
    }

    @NTValue(weight = 0)
    public double getValue() {
        return number;
    }
}
