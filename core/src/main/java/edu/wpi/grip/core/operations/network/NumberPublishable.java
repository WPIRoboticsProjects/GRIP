package edu.wpi.grip.core.operations.network;

import edu.wpi.grip.annotation.operation.PublishableObject;

import javax.annotation.concurrent.Immutable;

/**
 * An adapter to allow numbers to be published from GRIP sockets into a {@link NetworkPublisher}.
 *
 * @see PublishAnnotatedOperation#PublishAnnotatedOperation
 */
@Immutable
@PublishableProxy(Number.class)
@PublishableObject
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
