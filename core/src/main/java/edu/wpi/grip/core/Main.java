package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.generated.CVOperations;

import java.io.File;

/**
 * Main driver class for headless mode
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: GRIP.jar project.grip");
            return;
        }

        final String projectPath = args[0];

        final EventBus eventBus = new EventBus((exception, context) -> exception.printStackTrace());
        final Pipeline pipeline = new Pipeline(eventBus);
        final Palette palette = new Palette(eventBus);
        final Project project = new Project(eventBus, pipeline, palette);

        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);

        // Open a project from a .grip file specified on the command line
        project.open(new File(projectPath));

        // There's nothing more to do in the main thread since we're in headless mode - sleep forever
        for (; ; ) {
            Thread.sleep(Integer.MAX_VALUE);
        }
    }
}
