package edu.wpi.grip.core;

import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.HttpPipelineSwitcher;
import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.network.GripNetworkModule;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.sources.GripSourcesHardwareModule;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Main driver class for headless mode.
 */
public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  @Inject
  private Project project;
  @Inject
  private PipelineRunner pipelineRunner;
  @Inject
  private EventBus eventBus;
  @Inject
  private Operations operations;
  @Inject
  private CVOperations cvOperations;
  @Inject
  private GripServer gripServer;
  @Inject
  private HttpPipelineSwitcher pipelineSwitcher;

  @SuppressWarnings("JavadocMethod")
  public static void main(String[] args) throws IOException, InterruptedException {
    final Injector injector = Guice.createInjector(Modules.override(new GripCoreModule(),
        new GripFileModule(), new GripSourcesHardwareModule()).with(new GripNetworkModule()));
    injector.getInstance(Main.class).start(args);
  }

  @SuppressWarnings("JavadocMethod")
  public void start(String[] args) throws IOException, InterruptedException {
    String projectPath = null;
    if (args.length == 1) {
      logger.log(Level.INFO, "Loading file " + args[0]);
      projectPath = args[0];
    }

    operations.addOperations();
    cvOperations.addOperations();
    gripServer.addHandler(pipelineSwitcher);
    gripServer.start();

    // Open a project from a .grip file specified on the command line
    if (projectPath != null) {
      project.open(new File(projectPath));
    }

    pipelineRunner.startAsync();

    // This is done in order to indicate to the user using the deployment UI that this is running
    logger.log(Level.INFO, "SUCCESS! The project is running in headless mode!");
    // There's nothing more to do in the main thread since we're in headless mode - sleep forever
    while (true) {
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
    Logger.getLogger(event.getOrigin().getClass().getName()).log(Level.INFO, "Exception Cleared "
        + "Event");
  }

}