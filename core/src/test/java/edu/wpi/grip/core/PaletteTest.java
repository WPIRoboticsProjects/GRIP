package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.OperationAddedEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PaletteTest {
    private Palette palette;
    private EventBus eventBus;
    private Operation operation;

    @Before
    public void setUp() {
        eventBus = new EventBus();
        palette = new Palette(eventBus);
        eventBus.register(palette);
        operation = new Operation() {
            @Override
            public String getName() {
                return "Find Target";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public InputSocket<?>[] createInputSockets(EventBus eventBus) {
                return new InputSocket<?>[0];
            }

            @Override
            public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
                return new OutputSocket<?>[0];
            }

            @Override
            public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
            }
        };
    }

    @Test
    public void testGetOperation() {
        eventBus.post(new OperationAddedEvent(operation));
        assertEquals(Optional.of(operation), palette.getOperationByName("Find Target"));
    }

    @Test
    public void testGetAllOperations() {
        eventBus.post(new OperationAddedEvent(operation));
        assertEquals(Collections.singleton(operation), new HashSet<>(palette.getOperations()));
    }

    @Test
    public void testGetNonexistantOperation() {
        eventBus.post(new OperationAddedEvent(operation));
        assertEquals(Optional.empty(), palette.getOperationByName("Test"));
    }
}
