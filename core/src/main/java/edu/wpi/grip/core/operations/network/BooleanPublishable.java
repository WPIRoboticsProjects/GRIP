package edu.wpi.grip.core.operations.network;

import javax.annotation.concurrent.Immutable;

/**
 * An adapter to allow booleans to be published from GRIP sockets into a {@link NetworkPublisher}.
 */
@Immutable
public final class BooleanPublishable implements Publishable {
  private final boolean bool;

  public BooleanPublishable(Boolean bool) {
    this.bool = bool.booleanValue();
  }

  @PublishValue(weight = 1)
  public boolean getValue() {
    return bool;
  }
}
