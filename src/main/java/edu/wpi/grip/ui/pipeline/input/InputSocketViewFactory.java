package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.SocketHint;

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
                return new InputSocketView<T>(eventBus, socket) {};

            case SPINNER:
                if (socketHint.getType().equals(Number.class)) {
                    return (InputSocketView<T>) new NumberSpinnerInputSocketView(eventBus, (InputSocket<Number>) socket);
                } else {
                    return new ListSpinnerInputSocketView<>(eventBus, socket);
                }

            case SLIDER:
                if (socketHint.getType().equals(Number.class)) {
                    return (InputSocketView<T>) new SliderInputSocketView(eventBus, (InputSocket<Number>) socket);
                }

                break;

            case RANGE:
                // TODO
                break;

            case SELECT:
                // TODO
                break;

            case CHECKBOX:
                if (socketHint.getType().equals(Boolean.class)) {
                    return (InputSocketView<T>) new CheckboxInputSocketView(eventBus, (InputSocket<Boolean>) socket);
                }

                break;
        }

        throw new RuntimeException("Could not create view for socket: " + socket.toString());
    }
}
