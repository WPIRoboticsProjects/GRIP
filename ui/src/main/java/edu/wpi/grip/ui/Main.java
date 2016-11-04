package edu.wpi.grip.ui;

import edu.wpi.grip.core.CoreCommandLineHelper;
import edu.wpi.grip.core.GripCoreModule;
import edu.wpi.grip.core.GripFileModule;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.HttpPipelineSwitcher;
import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.network.GripNetworkModule;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.sources.GripSourcesHardwareModule;
import edu.wpi.grip.core.util.SafeShutdown;
import edu.wpi.grip.ui.util.DPIUtility;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.sun.javafx.application.PlatformImpl;

import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.inject.Inject;

public class Main extends Application {

  private final Object dialogLock = new Object();
  private static final Logger logger = Logger.getLogger(Main.class.getName());
  private static final String MAIN_TITLE = "GRIP Computer Vision Engine";

  /**
   * JavaFX insists on creating the main application with its own reflection code, so we can't
   * create with the Guice and do automatic field injection. However, we can inject it after the
   * fact.
   */
  @VisibleForTesting
  protected Injector injector;
  @Inject private EventBus eventBus;
  @Inject private PipelineRunner pipelineRunner;
  @Inject private Project project;
  @Inject private Operations operations;
  @Inject private CVOperations cvOperations;
  @Inject private GripServer server;
  @Inject private HttpPipelineSwitcher pipelineSwitcher;
  private Parent root;
  private boolean headless;
  private CommandLine parsedArgs;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() throws IOException {
    UICommandLineHelper commandLineHelper = new UICommandLineHelper();
    parsedArgs = commandLineHelper.parse(getParameters().getRaw());

    if (parsedArgs.hasOption(UICommandLineHelper.HEADLESS_OPTION)) {
      // If --headless was specified on the command line,
      // run in headless mode (only use the core module)
      injector = Guice.createInjector(Modules.override(new GripCoreModule(), new GripFileModule(),
          new GripSourcesHardwareModule()).with(new GripNetworkModule()));
      injector.injectMembers(this);

      headless = true;
    } else {
      // Otherwise, run with both the core and UI modules, and show the JavaFX stage
      injector = Guice.createInjector(Modules.override(new GripCoreModule(), new GripFileModule(),
          new GripSourcesHardwareModule()).with(new GripNetworkModule(), new GripUiModule()));
      injector.injectMembers(this);
      notifyPreloader(new Preloader.ProgressNotification(0.15));

      System.setProperty("prism.lcdtext", "false");
      Font.loadFont(this.getClass().getResource("roboto/Roboto-Regular.ttf").openStream(), -1);
      Font.loadFont(this.getClass().getResource("roboto/Roboto-Bold.ttf").openStream(), -1);
      Font.loadFont(this.getClass().getResource("roboto/Roboto-Italic.ttf").openStream(), -1);
      Font.loadFont(this.getClass().getResource("roboto/Roboto-BoldItalic.ttf").openStream(), -1);
      notifyPreloader(new Preloader.ProgressNotification(0.3));
    }

    notifyPreloader(new Preloader.ProgressNotification(0.45));
    server.addHandler(pipelineSwitcher);
    notifyPreloader(new Preloader.ProgressNotification(0.6));

    pipelineRunner.startAsync();
    notifyPreloader(new Preloader.ProgressNotification(0.75));
  }

  @Override
  public void start(Stage stage) throws IOException {
    // Load UI elements if we're not in headless mode
    if (!headless) {
      root = FXMLLoader.load(Main.class.getResource("MainWindow.fxml"), null, null,
          injector::getInstance);
      root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

      notifyPreloader(new Preloader.ProgressNotification(0.9));

      project.addIsSaveDirtyConsumer(newValue -> {
        if (newValue) {
          Platform.runLater(() -> stage.setTitle(MAIN_TITLE + " | Edited"));
        } else {
          Platform.runLater(() -> stage.setTitle(MAIN_TITLE));
        }
      });

      // If this isn't here this can cause a deadlock on windows. See issue #297
      stage.setOnCloseRequest(event -> SafeShutdown.exit(0, Platform::exit));
      stage.setTitle(MAIN_TITLE);
      stage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
      stage.setScene(new Scene(root));
      notifyPreloader(new Preloader.ProgressNotification(1.0));
      notifyPreloader(new Preloader.StateChangeNotification(
          Preloader.StateChangeNotification.Type.BEFORE_START));
      stage.show();
    }

    operations.addOperations();
    cvOperations.addOperations();

    // If there was a file specified on the command line, open it immediately
    if (parsedArgs.hasOption(CoreCommandLineHelper.FILE_OPTION)) {
      String file = parsedArgs.getOptionValue(CoreCommandLineHelper.FILE_OPTION);
      try {
        project.open(new File(file));
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error loading file: " + file);
        throw e;
      }
    }

    // Set the port AFTER loading the project to override the setting in the file
    if (parsedArgs.hasOption(CoreCommandLineHelper.PORT_OPTION)) {
      try {
        int port = Integer.parseInt(parsedArgs.getOptionValue(CoreCommandLineHelper.PORT_OPTION));
        if (port < 1024 || port > 65535) {
          logger.warning("Not a valid port: " + port);
        } else {
          // Valid port; set it (Note: this doesn't check to see if the port is available)
          logger.info("Running server on port " + port);
          server.setPort(port);
        }
      } catch (NumberFormatException e) {
        logger.warning(
            "Not a valid port: " + parsedArgs.getOptionValue(CoreCommandLineHelper.PORT_OPTION));
      }
    }

    // This will throw an exception if the port specified by the save file or command line
    // argument is already taken. Since we have to have the server running to handle remotely
    // loading pipelines and uploading images, as well as potential HTTP publishing operations,
    // this will cause the program to exit.
    server.start();
  }

  public void stop() {
    SafeShutdown.flagStopping();
  }

  @Subscribe
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
    event.handleSafely((throwable, message, isFatal) -> {
      // Check this so we can avoid entering the the platform wait
      // if the program is shutting down.
      if (!SafeShutdown.isStopping()) {
        // This should still use PlatformImpl
        PlatformImpl.runAndWait(() -> {
          // WARNING! Do not post any events from within this! It could result in a deadlock!
          synchronized (this.dialogLock) {
            // Check again because the value could have been changed while waiting for the javafx
            // thread to run.
            if (!SafeShutdown.isStopping()) {
              try {
                // Don't create more than one exception dialog at the same time
                final ExceptionAlert exceptionAlert = new ExceptionAlert(root, throwable,
                    message, isFatal, getHostServices());
                exceptionAlert.setInitialFocus();
                exceptionAlert.showAndWait();
              } catch (Throwable e) {
                // Well in this case something has gone very, very wrong
                // We don't want to create a feedback loop either.
                try {
                  logger.log(Level.SEVERE, "Failed to show exception alert", e);
                } finally {
                  SafeShutdown.exit(1); // Ensure we shut down the application if we get an
                  // exception
                }
              }
            }
          }
        });
      } else {
        logger.log(Level.INFO, "Did not display exception because UI was stopping", throwable);
      }
    });
  }
}
