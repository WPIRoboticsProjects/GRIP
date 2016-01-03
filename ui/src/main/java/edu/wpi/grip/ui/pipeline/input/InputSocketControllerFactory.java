package edu.wpi.grip.ui.pipeline.input;

import com.google.inject.Inject;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.SocketHint;

import java.util.List;

/**
 * Factory for constructing editable controls for input sockets
 */
public final class InputSocketControllerFactory {

    @Inject
    private InputSocketController.BaseInputSocketControllerFactory<Object> baseInputSocketControllerFactory;
    @Inject
    private CheckboxInputSocketController.Factory checkboxInputSocketControllerFactory;
    @Inject
    private NumberSpinnerInputSocketController.Factory numberInputSocketControllerFactory;
    @Inject
    private ListSpinnerInputSocketController.Factory listInputSocketControllerFactory;
    @Inject
    private SliderInputSocketController.Factory sliderInputSocketControllerFactory;
    @Inject
    private RangeInputSocketController.Factory rangeInputSocketControllerFactory;
    @Inject
    private SelectInputSocketController.Factory<Object> selectInputSocketControllerFactory;
    @Inject
    private TextFieldInputSocketController.Factory textFieldInputSocketController;

    /**
     * Create an instance of {@link InputSocketController} appropriate for the given socket.
     */
    @SuppressWarnings("unchecked")
    public <T> InputSocketController<T> create(InputSocket<T> socket) {
        final SocketHint<T> socketHint = socket.getSocketHint();

        switch (socketHint.getView()) {
            case NONE:
                return (InputSocketController<T>) baseInputSocketControllerFactory.create((InputSocket<Object>) socket);

            case TEXT:
                if (socketHint.getType().equals(Number.class)) {
                    return (InputSocketController<T>) numberInputSocketControllerFactory.create((InputSocket<Number>) socket);
                } else if (socketHint.getType().equals(List.class)) {
                    return (InputSocketController<T>) listInputSocketControllerFactory.create((InputSocket<List>) socket);
                } else if (socketHint.getType().equals(String.class)) {
                    return (InputSocketController<T>) textFieldInputSocketController.create((InputSocket<String>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  SPINNER views must be Number or List. "
                            + socket.toString());
                }

            case SLIDER:
                if (socketHint.getType().equals(Number.class)) {
                    return (InputSocketController<T>) sliderInputSocketControllerFactory.create((InputSocket<Number>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  SLIDER views must be Numbers. "
                            + socket.toString());
                }

            case RANGE:
                if (socketHint.getType().equals(List.class)) {
                    return (InputSocketController<T>) rangeInputSocketControllerFactory.create((InputSocket<List<Number>>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  RANGE views must be Lists. "
                            + socket.toString());
                }

            case SELECT:
                return (InputSocketController<T>) selectInputSocketControllerFactory.create((InputSocket<Object>) socket);

            case CHECKBOX:
                if (socketHint.getType().equals(Boolean.class)) {
                    return (InputSocketController<T>) checkboxInputSocketControllerFactory.create((InputSocket<Boolean>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  CHECKBOX views must be Booleans. "
                            + socket.toString());
                }

            default:
                throw new IllegalArgumentException("Could not create view for socket. " + socket.toString());
        }

    }
}
