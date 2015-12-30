package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.generated.CVOperations;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Main driver class for headless mode
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: GRIP.jar project.grip");
            return;
        }

        //Set up the global level logger. This handles IO for all loggers.
        Logger globalLogger = LogManager.getLogManager().getLogger("");//This is our global logger

        Handler fileHandler = null;//This will be our handler for the global logger

        try {
            fileHandler = new FileHandler("./GRIP.log");//Log to the file "GRIPlogger.log"

            globalLogger.addHandler(fileHandler);//Add the handler to the global logger

            fileHandler.setFormatter(new SimpleFormatter());//log in text, not xml

            //Set level to handler and logger
            fileHandler.setLevel(Level.FINE);
            globalLogger.setLevel(Level.FINE);

            globalLogger.config("Configuration done.");//Log that we are done setting up the logger

        } catch (IOException exception) {//Something happened setting up file IO
            throw new IllegalStateException(exception);
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
