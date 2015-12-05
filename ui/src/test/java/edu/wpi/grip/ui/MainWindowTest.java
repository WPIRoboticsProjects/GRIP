package edu.wpi.grip.ui;


import edu.wpi.grip.core.AdditionOperation;
import edu.wpi.grip.core.events.OperationAddedEvent;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import static org.testfx.api.FxAssert.verifyThat;


public class MainWindowTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);

        final PaletteView palette = lookup(".palette").<PaletteView>queryFirst();
        palette.clearOperations();
        palette.onOperationAdded(new OperationAddedEvent(new AdditionOperation()));
    }

    @Test
    public void testShouldCreateNewOperationInPipelineView() {
        // Given:
        clickOn("#add-operation");

        // Then:
        verifyThat(".pipeline", NodeMatchers.hasChild(".add-step"));
    }


}