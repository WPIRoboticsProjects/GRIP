package edu.wpi.grip.ui.components;

import edu.wpi.grip.core.PreviousNext;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static junit.framework.TestCase.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

public class PreviousNextButtonsTest extends ApplicationTest {

    private MockPreviousNext previousNext;
    private PreviousNextButtons previousNextButtons;

    class MockPreviousNext implements PreviousNext {

        private int index;

        private MockPreviousNext() {
            this.index = 0;
        }

        @Override
        public void next() {
            index++;
        }

        @Override
        public void previous() {
            index--;
        }
    }

    @Override
    public void start(Stage stage) {
        previousNext = new MockPreviousNext();
        previousNextButtons = new PreviousNextButtons(previousNext);
        Scene scene = new Scene(previousNextButtons, 800, 600);
        stage.setScene(scene);
        stage.show();

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testInitialState() {
        verifyThat(previousNextButtons, NodeMatchers.hasChild("." + PreviousNextButtons.NEXT_BUTTON_STYLE_CLASS));
        verifyThat(previousNextButtons, NodeMatchers.hasChild("." + PreviousNextButtons.PREVIOUS_BUTTON_STYLE_CLASS));
    }

    @Test
    @Ignore("Fails on TravisCI") // TODO: Figure out what is different on travis that causes this to fail.
    public void testClickPrevious() {
        clickOn(previousNextButtons.getPreviousButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Previous should have been called once", -1, previousNext.index);
        verifyThat("." + PreviousNextButtons.PREVIOUS_BUTTON_STYLE_CLASS, (ToggleButton t) -> !t.isSelected());
    }

    @Test
    @Ignore("Fails everywhere") // TODO: Figure out why this breaks everywhere.
    public void testClickNext() {
        clickOn(previousNextButtons.getNextButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Next should have been called once", 1, previousNext.index);
        verifyThat("." + PreviousNextButtons.NEXT_BUTTON_STYLE_CLASS, (ToggleButton t) -> !t.isSelected());
    }

}