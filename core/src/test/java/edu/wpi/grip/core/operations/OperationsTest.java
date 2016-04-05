package edu.wpi.grip.core.operations;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.util.MockExceptionWitness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import edu.wpi.grip.generated.CVOperations;

public class OperationsTest {
    private List<OperationMetaData> operationList;
    private Optional<Throwable> throwableOptional;
    private EventBus eventBus;

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
        OperationsFactory.createCV(eventBus).addOperations();
        for (OperationMetaData operationMeta : operationList) {
            final Step step =
                    new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(operationMeta);
            step.setRemoved();
        }
    }

    @Test
    public void testCreateAllCoreSteps() {
        OperationsFactory.create(eventBus)
                .addOperations();
        for (OperationMetaData operationMeta : operationList) {
            final Step step =
                    new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(operationMeta);
            step.setRemoved();
        }
    }
}
