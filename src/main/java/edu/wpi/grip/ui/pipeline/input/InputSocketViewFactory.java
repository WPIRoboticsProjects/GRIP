package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.SocketHint;

import java.util.List;

/**
 * Factory for constructing editable controls for input sockets
 */
public class InputSocketViewFactory {

    /**
     * Create an instance of {@link InputSocketView} appropriate for the given socket.
     */
    @SuppressWarnings("unchecked")
    public static <T> InputSocketView<T> createInputSocketView(EventBus eventBus, InputSocket<T> socket) {
        final SocketHint<T> socketHint = socket.getSocketHint();

        switch (socketHint.getView()) {
            case NONE:
                return new InputSocketView<T>(eventBus, socket) {
                };

            case SPINNER:
                if (socketHint.getType().equals(Number.class)) {
                    return (InputSocketView<T>) new NumberSpinnerInputSocketView(eventBus, (InputSocket<Number>) socket);
                } else {
                    return new ListSpinnerInputSocketView<>(eventBus, socket);
                }

            case SLIDER:
                if (socketHint.getType().equals(Number.class)) {
                    return (InputSocketView<T>) new SliderInputSocketView(eventBus, (InputSocket<Number>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  SLIDER views must be Numbers. "
                            + socket.toString());
                }

            case RANGE:
                if (socketHint.getType().equals(List.class)) {
                    return (InputSocketView<T>) new RangeInputSocketView(eventBus, (InputSocket<List<Number>>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  RANGE views must be Lists. "
                            + socket.toString());
                }


            case SELECT:
                return new SelectInputSocketView<>(eventBus, socket);

            case CHECKBOX:
                if (socketHint.getType().equals(Boolean.class)) {
                    return (InputSocketView<T>) new CheckboxInputSocketView(eventBus, (InputSocket<Boolean>) socket);
                } else {
                    throw new IllegalArgumentException("Could not create view for socket.  CHECKBOX views must be Booleans. "
                            + socket.toString());
                }

            default:
                throw new IllegalArgumentException("Could not create view for socket. " + socket.toString());
        }

    }
}
