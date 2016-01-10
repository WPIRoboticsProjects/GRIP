package edu.wpi.grip.core.operations;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.networktables.MockNTManager;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.generated.CVOperations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperationsTest {
    private List<Operation> operationList;
    private Optional<Throwable> throwableOptional;
    private EventBus eventBus;

    public static class OperationsWithMockNTManager extends Operations {
        public OperationsWithMockNTManager() {
            super(new MockNTManager());
        }
    }

    private class OperationGrabber {
        @Subscribe
        public void onOperationAddedEvent(OperationAddedEvent event) {
            operationList.add(event.getOperation());
        }
    }


    @Before
    public void setUp() {
        this.operationList = new ArrayList<>();
        this.throwableOptional = Optional.empty();
        this.eventBus = new EventBus((exception, context) -> throwableOptional = Optional.of(exception));

        this.eventBus.register(new OperationGrabber());
    }

    @After
    public void afterTest() {
        if (throwableOptional.isPresent()) {
            throw Throwables.propagate(throwableOptional.get());
        }
    }

    @Test
    public void testCreateAllCVSteps() {
        CVOperations.addOperations(eventBus);
        for (Operation operation : operationList) {
            new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(operation);
        }
    }

    @Test
    public void testCreateAllCoreSteps() {
        new OperationsWithMockNTManager().addOperations(eventBus);
        for (Operation operation : operationList) {
            new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(operation);
        }
    }
}
