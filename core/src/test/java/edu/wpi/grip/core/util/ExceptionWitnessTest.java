package edu.wpi.grip.core.util;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ExceptionWitnessTest {
    private ExceptionWitness errorWitness;
    private Object witnessObserver;
    private TestWitnessListener testWitnessListener;

    /**
     * Used to count the number of calls that ExceptionEvent and ExceptionClearedEvent fires.
     */
    private class TestWitnessListener {
        private int errorRunCount = 0;
        private int clearRunCount = 0;
        private Optional<Object> errorWitnessObserver = Optional.empty();
        private Optional<Object> clearedWitnessObserver = Optional.empty();

        @Subscribe
        public void onErrorEvent(ExceptionEvent event) {
            errorRunCount++;
            this.errorWitnessObserver = Optional.of(event.getOrigin());
        }

        @Subscribe
        public void onErrorClearedEvent(ExceptionClearedEvent event) {
            clearRunCount++;
            this.clearedWitnessObserver = Optional.of(event.getOrigin());
        }
    }

    @Before
    public void setUp() {
        final EventBus eventBus = new EventBus();
        this.witnessObserver = new Object();
        this.errorWitness = new ExceptionWitness(eventBus, witnessObserver);
        this.testWitnessListener = new TestWitnessListener();
        eventBus.register(testWitnessListener);
    }

    @Test
    public void testErrorSameOrigin() {
        fireAnError();
        assertTrue("The witness observer was not present", testWitnessListener.errorWitnessObserver.isPresent());
        assertEquals("The error run count was not 1", 1, testWitnessListener.errorRunCount);
        assertEquals("Witness observer was not the same as the observer from the error event", witnessObserver, testWitnessListener.errorWitnessObserver.get());
    }

    @Test
    public void testClearNoErrorCallCount() {
        errorWitness.clearException();
        assertEquals("No error was posted but clear fired a clear event", 0, testWitnessListener.clearRunCount);
    }

    @Test
    public void testErrorThenClearCallCount() {
        fireAnError();
        errorWitness.clearException();
        assertTrue("The cleared witness observer was not present", testWitnessListener.clearedWitnessObserver.isPresent());
        assertEquals("The cleared run count was not 1", 1, testWitnessListener.clearRunCount);
        assertEquals("Witness observer was not the same as the observer from the cleared event", witnessObserver, testWitnessListener.clearedWitnessObserver.get());
    }

    @Test
    public void testErrorThenClearMultipleCallCount() {
        fireAnError();
        for (int i = 0; i < 10; i++) {
            errorWitness.clearException();
        }
        assertEquals("The cleared run count was not 1 after multiple clearException calls without an error call between them", 1, testWitnessListener.clearRunCount);
    }


    private void fireAnError() {
        try {
            throw new IllegalStateException("This is expected");
        } catch (IllegalStateException e) {
            errorWitness.flagException(e);
        }
    }
}