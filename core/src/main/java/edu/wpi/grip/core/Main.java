package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;
import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.network.GRIPNetworkModule;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.sources.GRIPSourcesHardwareModule;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main driver class for headless mode
 */
public class Main {

    @Inject private Project project;
    @Inject private PipelineRunner pipelineRunner;
    @Inject private EventBus eventBus;
    @Inject private Operations operations;
    @Inject private CVOperations cvOperations;
    @Inject private Logger logger;

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(String[] args) throws IOException, InterruptedException {
        final Injector injector = Guice.createInjector(new GRIPCoreModule(), new GRIPNetworkModule(), new GRIPSourcesHardwareModule());
        injector.getInstance(Main.class).start(args);
    }

    @SuppressWarnings("PMD.SystemPrintln")
    public void start(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: GRIP.jar project.grip");
            return;
        } else {
            logger.log(Level.INFO, "Loading file " + args[0]);
        }

        operations.addOperations();
        cvOperations.addOperations();

        final String projectPath = args[0];

        // Open a project from a .grip file specified on the command line
        project.open(new File(projectPath));

        pipelineRunner.startAsync();

        // This is done in order to indicate to the user using the deployment UI that this is running
        logger.log(Level.INFO, "SUCCESS! The project is running in headless mode!");
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

}
