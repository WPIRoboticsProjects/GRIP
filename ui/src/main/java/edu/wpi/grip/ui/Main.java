package edu.wpi.grip.ui;

import edu.wpi.grip.core.GripCoreModule;
import edu.wpi.grip.core.GripCudaModule;
import edu.wpi.grip.core.GripFileModule;
import edu.wpi.grip.core.Loggers;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.cuda.CudaVerifier;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
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
import edu.wpi.grip.ui.util.DPIUtility;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.sun.javafx.application.PlatformImpl;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
  @Inject private SettingsProvider settingsProvider;
  @Inject private Operations operations;
  @Inject private CVOperations cvOperations;
  @Inject private GripServer server;
  @Inject private HttpPipelineSwitcher pipelineSwitcher;
  private Parent root;
  private boolean headless;
  private final UICommandLineHelper commandLineHelper = new UICommandLineHelper();
  private CommandLine parsedArgs;

  @Override
  public void init() throws IOException {
    Loggers.setupLoggers();
    parsedArgs = commandLineHelper.parse(getParameters().getRaw());

    // Verify CUDA before using the core module, since that will cause OpenCV to be loaded,
    // which will crash the app if we use CUDA and it's not available
    GripCudaModule cudaModule = new GripCudaModule();
    CudaVerifier cudaVerifier = Guice.createInjector(cudaModule).getInstance(CudaVerifier.class);
    cudaVerifier.verifyCuda();

    if (parsedArgs.hasOption(UICommandLineHelper.HEADLESS_OPTION)) {
      // If --headless was specified on the command line,
      // run in headless mode (only use the core module)
      logger.info("Launching GRIP in headless mode");
      injector = Guice.createInjector(
          Modules.override(
              new GripCoreModule(),
              new GripFileModule(),
              new GripSourcesHardwareModule()
          ).with(new GripNetworkModule(), cudaModule));
      injector.injectMembers(this);

      headless = true;
    } else {
      // Otherwise, run with both the core and UI modules, and show the JavaFX stage
      logger.info("Launching GRIP in UI mode");
      injector = Guice.createInjector(
          Modules.override(
              new GripCoreModule(),
              new GripFileModule(),
              new GripSourcesHardwareModule()
          ).with(new GripNetworkModule(), new GripUiModule(), cudaModule));
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

    commandLineHelper.loadFile(parsedArgs, project);
    commandLineHelper.setServerPort(parsedArgs, settingsProvider, eventBus);

    // This will throw an exception if the port specified by the save file or command line
    // argument is already taken. Since we have to have the server running to handle remotely
    // loading pipelines and uploading images, as well as potential HTTP publishing operations,
    // this will cause the program to exit.
    try {
      server.start();
    } catch (GripServerException e) {
      logger.log(Level.SEVERE, "The HTTP server could not be started", e);
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
      alert.setTitle("The HTTP server could not be started");
      alert.setHeaderText("The HTTP server could not be started");
      alert.setContentText(
          "This is normally caused by the network port being used by another process.\n\n"
              + "HTTP sources and operations will not work until GRIP is restarted. "
              + "Continue without HTTP functionality anyway?"
      );
      alert.showAndWait()
          .filter(ButtonType.NO::equals)
          .ifPresent(bt -> SafeShutdown.exit(SafeShutdown.ExitCode.HTTP_SERVER_COULD_NOT_START));
    }
  }

  @Override
  public void stop() {
    SafeShutdown.flagStopping();
  }

  @Subscribe
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
    event.handleSafely((throwable, message, isFatal) -> {
      // Check this so we can avoid entering the the platform wait
      // if the program is shutting down.
      if (!SafeShutdown.isStopping()) { // NOPMD
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
                  // Ensure we shut down the application if we get an exception
                  SafeShutdown.exit(SafeShutdown.ExitCode.MISC_ERROR);
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
