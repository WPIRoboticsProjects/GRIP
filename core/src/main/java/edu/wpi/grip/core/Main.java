package edu.wpi.grip.core;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.serialization.Project;

import javax.inject.Inject;
import java.io.File;

/**
 * Main driver class for headless mode
 */
public class Main {

    @Inject private Project project;

    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new GRIPCoreModule());
        injector.getInstance(Main.class).start(args);
    }

    public void start(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: GRIP.jar project.grip");
            return;
        }

        final String projectPath = args[0];

        // Open a project from a .grip file specified on the command line
        project.open(new File(projectPath));

        // There's nothing more to do in the main thread since we're in headless mode - sleep forever
        for (; ; ) {
            Thread.sleep(Integer.MAX_VALUE);
        }
    }

    /**
     * When an unexpected error happens in headless mode, print a stack trace and exit.  Obviously we cannot show
     * a dialog here, so this will have to do until we have proper logging.
     * <p>
     * TODO: put actual logging in this method
     */
    @Subscribe
    public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
        System.out.println(event.getMessage());
        event.getThrowable().printStackTrace();
        if (event.isFatal()) {
            System.exit(1);
        }
    }
}
