package edu.wpi.grip.ui.pipeline.input;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import com.google.inject.Inject;

import java.util.List;

/**
 * Factory for constructing editable controls for input sockets.
 */
public final class InputSocketControllerFactory {

  @Inject
  private InputSocketController.BaseInputSocketControllerFactory<Object> baseSocketFactory;
  @Inject
  private CheckboxInputSocketController.Factory checkboxSocketFactory;
  @Inject
  private NumberSpinnerInputSocketController.Factory numberSocketFactory;
  @Inject
  private ListSpinnerInputSocketController.Factory listSocketFactory;
  @Inject
  private SliderInputSocketController.Factory sliderSocketFactory;
  @Inject
  private RangeInputSocketController.Factory rangeSocketFactory;
  @Inject
  private SelectInputSocketController.Factory<Object> selectSocketFactory;
  @Inject
  private TextFieldInputSocketController.Factory textFieldSocketFactory;

  /**
   * Create an instance of {@link InputSocketController} appropriate for the given socket.
   */
  @SuppressWarnings("unchecked")
  public <T> InputSocketController<T> create(InputSocket<T> socket) {
    final SocketHint<T> socketHint = socket.getSocketHint();

    switch (socketHint.getView()) {
      case NONE:
        return (InputSocketController<T>) baseSocketFactory.create(
            (InputSocket<Object>) socket);
      case TEXT:
        return makeTextSocketController(socket, socketHint);

      case SLIDER:
        return makeSliderSocketController(socket, socketHint);

      case RANGE:
        return makeRangeSocketController(socket, socketHint);

      case SELECT:
        return (InputSocketController<T>) selectSocketFactory.create(
            (InputSocket<Object>) socket);

      case CHECKBOX:
        return makeCheckboxSocketController(socket, socketHint);

      default:
        throw new IllegalArgumentException("Could not create view for socket. " + socket);
    }

  }

  @SuppressWarnings("unchecked")
  private <T> InputSocketController<T> makeTextSocketController(InputSocket<T> socket,
                                                                SocketHint<T> socketHint) {
    if (socketHint.getType().equals(Number.class)) {
      return (InputSocketController<T>) numberSocketFactory.create(
          (InputSocket<Number>) socket);
    } else if (socketHint.getType().equals(List.class)) {
      return (InputSocketController<T>) listSocketFactory.create(
          (InputSocket<List>) socket);
    } else if (socketHint.getType().equals(String.class)) {
      return (InputSocketController<T>) textFieldSocketFactory.create(
          (InputSocket<String>) socket);
    } else {
      throw new IllegalArgumentException("Could not create view for socket.  SPINNER views "
          + "must be Number or List. "
          + socket.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private <T> InputSocketController<T> makeSliderSocketController(InputSocket<T> socket,
                                                                  SocketHint<T> socketHint) {
    if (socketHint.getType().equals(Number.class)) {
      return (InputSocketController<T>) sliderSocketFactory.create(
          (InputSocket<Number>) socket);
    } else {
      throw new IllegalArgumentException("Could not create view for socket.  SLIDER views "
          + "must be Numbers. " + socket);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> InputSocketController<T> makeRangeSocketController(InputSocket<T> socket,
                                                                 SocketHint<T> socketHint) {
    if (socketHint.getType().equals(List.class)) {
      return (InputSocketController<T>) rangeSocketFactory.create(
          (InputSocket<List<Number>>) socket);
    } else {
      throw new IllegalArgumentException("Could not create view for socket.  RANGE views must"
          + " be Lists. " + socket);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> InputSocketController<T> makeCheckboxSocketController(InputSocket<T> socket,
                                                                    SocketHint<T> socketHint) {
    if (socketHint.getType().equals(Boolean.class)) {
      return (InputSocketController<T>) checkboxSocketFactory.create(
          (InputSocket<Boolean>) socket);
    } else {
      throw new IllegalArgumentException("Could not create view for socket.  CHECKBOX views "
          + "must be Booleans. " + socket);
    }
  }
}
