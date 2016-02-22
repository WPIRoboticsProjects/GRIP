package edu.wpi.grip.core.operations.network;

import javax.annotation.concurrent.Immutable;
import java.util.function.Function;

/**
 * An adapter to allow numbers to be published from GRIP sockets into a {@link NetworkKeyValuePublisher}
 *
 * @see KeyValuePublishOperation#PublishOperation(Manager, Class, Class, Function)
 */
@Immutable
public final class NumberPublishable implements Publishable {

    private final double number;

    public NumberPublishable(Number number) {
        this.number = number.doubleValue();
    }

    @PublishValue(weight = 0)
    public double getValue() {
        return number;
    }
}
