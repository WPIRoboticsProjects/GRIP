package edu.wpi.grip.ui.components;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.MockExceptionWitness;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testfx.api.FxAssert.verifyThat;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class ExceptionWitnessResponderButtonTest extends ApplicationTest {

    private ExceptionWitness witness;

    @Override
    public void start(Stage stage) {
        final EventBus eventBus = new EventBus();
        final Object witnessed = new Object();
        witness = new MockExceptionWitness(eventBus, witnessed);
        final ExceptionWitnessResponderButton button = new ExceptionWitnessResponderButton(witnessed, "Test Button Popover");
        eventBus.register(button);
        Scene scene = new Scene(button, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testOnExceptionEvent() throws Exception {
        flagNewException();
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS,
                () -> NodeMatchers.isVisible().matches(lookup("." + ExceptionWitnessResponderButton.STYLE_CLASS).query()));
    }

    @Test
    public void testOnExceptionClearedEvent() throws Exception {
        flagNewException();
        witness.clearException();
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS,
                () -> NodeMatchers.isInvisible().matches(lookup("." + ExceptionWitnessResponderButton.STYLE_CLASS).query()));
    }

    @Test
    @Ignore("Broken on AppVeyor")
    public void testPopoverAppears() throws TimeoutException {
        flagNewException();
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS,
                () -> NodeMatchers.isVisible().matches(lookup("." + ExceptionWitnessResponderButton.STYLE_CLASS).query()));
        clickOn("." + ExceptionWitnessResponderButton.STYLE_CLASS);
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS,
                () -> NodeMatchers.isVisible().matches(lookup("." + ExceptionWitnessResponderButton.ExceptionPopOver.STYLE_CLASS).query()));
    }

    @Test
    public void testPopoverDoesNotHaveStackTracePaneWhenWarningIsFlagged() throws Exception {
        flagWarning();
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS,
                () -> NodeMatchers.isVisible().matches(lookup("." + ExceptionWitnessResponderButton.STYLE_CLASS).query()));
        clickOn("." + ExceptionWitnessResponderButton.STYLE_CLASS);
        verifyThat("Stack Trace", NodeMatchers.isInvisible());
    }

    private void flagNewException() {
        try {
            throw new IllegalStateException("Illegal State");
        } catch (IllegalStateException e) {
            witness.flagException(e, "An Illegal Message that is really, really, really, really, really, really, really, " +
                    "really, really, really, really, really, really, really, really, really, really, long!");
        }
    }

    private void flagWarning() {
        witness.flagWarning("A warning without a stacktrace");
    }

    @Test
    public void testNoNullPointerBeforeAddedToScene() {
        final EventBus eventBus = new EventBus();
        final Object witnessed = new Object();
        final ExceptionWitness witness = new MockExceptionWitness(eventBus, witnessed);

        final ExceptionWitnessResponderButton button = new ExceptionWitnessResponderButton(witnessed, "Test Button Popover");
        eventBus.register(button);
        witness.flagWarning("Warning message");
        witness.clearException();
        // We should not get a null pointer because of any of this
    }

}