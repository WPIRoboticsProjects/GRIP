package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

public class SocketHintTest {

    @Test(expected = ClassCastException.class)
    public void safeCastSocketOfInconpatibleType() {
        final SocketHint<Boolean> booleanSocketHint = SocketHints.createBooleanSocketHint("testBool", false);
        final SocketHint<Number> numberSocketHint = SocketHints.createNumberSocketHint("testNumb", 0);
        final InputSocket<Number> numberInputSocket = new InputSocket<>(new EventBus(), numberSocketHint);

         booleanSocketHint.safeCastSocket(numberInputSocket);
    }
}