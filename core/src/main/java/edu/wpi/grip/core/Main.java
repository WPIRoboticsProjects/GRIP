package edu.wpi.grip.core;

import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;
import edu.wpi.grip.core.exception.GripServerException;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.HttpPipelineSwitcher;
import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.network.GripNetworkModule;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.settings.SettingsProvider;
import edu.wpi.grip.core.sources.GripSourcesHardwareModule;
import edu.wpi.grip.core.util.SafeShutdown;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.apache.commons.cli.CommandLine;

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
  private SettingsProvider settingsProvider;
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
    new CoreCommandLineHelper().parse(args); // Check for help or version before doing anything else
    final Injector injector = Guice.createInjector(Modules.override(new GripCoreModule(),
        new GripFileModule(), new GripSourcesHardwareModule()).with(new GripNetworkModule()));
    injector.getInstance(Main.class).start(args);
  }

  @SuppressWarnings("JavadocMethod")
  public void start(String[] args) throws IOException, InterruptedException {

    operations.addOperations();
    cvOperations.addOperations();
    gripServer.addHandler(pipelineSwitcher);

    CoreCommandLineHelper commandLineHelper = new CoreCommandLineHelper();
    CommandLine parsedArgs = commandLineHelper.parse(args);

    commandLineHelper.loadFile(parsedArgs, project);
    commandLineHelper.setServerPort(parsedArgs, settingsProvider, eventBus);

    // This will throw an exception if the port specified by the save file or command line
    // argument is already taken. Since we have to have the server running to handle remotely
    // loading pipelines and uploading images, as well as potential HTTP publishing operations,
    // this will cause the program to exit.
    try {
      gripServer.start();
    } catch (GripServerException e) {
      logger.log(Level.SEVERE, "The HTTP server could not be started", e);
      SafeShutdown.exit(1);
    }

    if (pipelineRunner.state() == Service.State.NEW) {
      // Loading a project will start the pipeline, so only start it if a project wasn't specified
      // as a command line argument.
      pipelineRunner.startAsync();
    }

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
