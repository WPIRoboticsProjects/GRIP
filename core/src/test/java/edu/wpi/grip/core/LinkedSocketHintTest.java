package edu.wpi.grip.core;


import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkedSocketHintTest {
    private SocketHint<Boolean> booleanSocketHint;
    private SocketHint<Number> numberSocketHint;

    @Before
    public void setUp() {
        booleanSocketHint = SocketHints.createBooleanSocketHint("testBooleanHint", true);
        numberSocketHint = SocketHints.createNumberSocketHint("testNumberHint", 30);
    }

    @Test
    public void testConnectingAnyType() {
        final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(new EventBus());

        assertTrue("Boolean should be compatible with linkedSocketHint", linkedSocketHint.isCompatibleWith(booleanSocketHint));
        assertTrue("Number should be compatible with linkedSocketHint", linkedSocketHint.isCompatibleWith(numberSocketHint));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMakingConnectionOfTypeBooleanPreventsConnectionOfTypeNumber () {
        // Given
        final EventBus eventBus = new EventBus();
        final OutputSocket booleanOutputSocket = new OutputSocket(eventBus, booleanSocketHint);
        final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(eventBus);

        // When
        final InputSocket connectedLinkedInputSocket = linkedSocketHint.linkedInputSocket("A");

        final Connection connection = new Connection<>(eventBus, (outputSocket, inputSocket) -> true, booleanOutputSocket, connectedLinkedInputSocket);
        connectedLinkedInputSocket.addConnection(connection);

        // Then
        assertFalse("Linked Socket Hint should no longer support number types", linkedSocketHint.isCompatibleWith(numberSocketHint));
        assertTrue("Linked Socket Hint should still accept boolean types", linkedSocketHint.isCompatibleWith(booleanSocketHint));
    }
}