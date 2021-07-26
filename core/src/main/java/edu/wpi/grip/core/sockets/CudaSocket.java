package edu.wpi.grip.core.sockets;

import edu.wpi.grip.core.cuda.CudaDetector;

import com.google.common.eventbus.EventBus;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A type of input socket that lets an operation know that it should prefer to use a
 * CUDA-accelerated code path. If no compatible CUDA runtime is available, sockets of this type will
 * <i>always</i> have a value of {@code false} and cannot be changed.
 */
public class CudaSocket extends InputSocketImpl<Boolean> {

  private static final Optional<Boolean> NO = Optional.of(false);

  private final boolean isCudaAvailable;

  CudaSocket(EventBus eventBus, CudaDetector detector, SocketHint<Boolean> socketHint) {
    super(eventBus, socketHint);

    // Cache the value, since it's not likely that the user will install or uninstall a CUDA
    // runtime while the app is running
    isCudaAvailable = detector.isCompatibleCudaInstalled();
  }

  @Override
  public Optional<Boolean> getValue() {
    if (isCudaAvailable) {
      return super.getValue();
    } else {
      return NO;
    }
  }

  @Override
  public void setValue(@Nullable Boolean value) {
    if (isCudaAvailable) {
      super.setValue(value);
    } else {
      super.setValue(false);
    }
  }

  @Override
  public void setValueOptional(Optional<? extends Boolean> optionalValue) {
    Objects.requireNonNull(optionalValue, "optionalValue");
    if (isCudaAvailable) {
      super.setValueOptional(optionalValue);
    } else {
      super.setValueOptional(NO);
    }
  }

  public boolean isCudaAvailable() {
    return isCudaAvailable;
  }
}
