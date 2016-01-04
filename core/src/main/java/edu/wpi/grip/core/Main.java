package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.generated.CVOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Main driver class for headless mode
 */
public class Main {

    @Inject
    private Project project;
    @Inject
    private EventBus eventBus;
    @Inject
    private Logger logger;

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new GRIPCoreModule());
        injector.getInstance(Main.class).start(args);
    }

    public void start(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: GRIP.jar project.grip");
            return;
        }

        //Set up the global level logger. This handles IO for all loggers.
        Logger globalLogger = LogManager.getLogManager().getLogger("");//This is our global logger

        Handler fileHandler = null;//This will be our handler for the global logger

        try {
            fileHandler = new FileHandler("%h/GRIP.log");//Log to the file "GRIPlogger.log"

            globalLogger.addHandler(fileHandler);//Add the handler to the global logger

            fileHandler.setFormatter(new SimpleFormatter());//log in text, not xml

            //Set level to handler and logger
            fileHandler.setLevel(Level.FINE);
            globalLogger.setLevel(Level.FINE);

            globalLogger.config("Configuration done.");//Log that we are done setting up the logger

        } catch (IOException exception) {//Something happened setting up file IO
            throw new IllegalStateException(exception);
        }


        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);

        final String projectPath = args[0];

        // Open a project from a .grip file specified on the command line
        project.open(new File(projectPath));

        // There's nothing more to do in the main thread since we're in headless mode - sleep forever
        for (; ; ) {
            Thread.sleep(Integer.MAX_VALUE);
        }
    }

    @Subscribe
    public final void onExceptionEvent(ExceptionEvent event) {
        Logger.getLogger(event.getOrigin().getClass().getName()).log(
                Level.SEVERE,
                event.getMessage(),
                // The throwable can be null
                event.getException().orElse(null)
        );
    }

    @Subscribe
    public final void onExceptionClearedEvent(ExceptionClearedEvent event) {
        Logger.getLogger(event.getOrigin().getClass().getName()).log(Level.INFO, "Exception Cleared Event");
    }

    /**
     * When an unexpected error happens in headless mode, print a stack trace and exit.
     */
    @Subscribe
    public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
        logger.log(Level.SEVERE, "UnexpectedThrowableEvent", event.getThrowable());
        if (event.isFatal()) {
            System.exit(1);
        }
    }
}
