package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.*;

public class Main extends Application {
    private final EventBus eventBus = new EventBus((exception, context) -> {
        this.triggerUnexpectedThrowableEvent(new UnexpectedThrowableEvent(exception, "An Event Bus subscriber threw an uncaught exception"));
    });

    private final Object dialogLock = new Object();
    private Parent root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        //Set up the global level logger. This handles IO for all loggers.
        Logger globalLogger = LogManager.getLogManager().getLogger("");//This is our global logger

        //Remove the default handler(s) so we can add our own
        Handler[] handlers = globalLogger.getHandlers();
        for(Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }

        Handler fileHandler  = null;//This will be our handler for the global logger

        try{
            fileHandler  = new FileHandler("./GRIPlogger.log");//Log to the file "GRIPlogger.log"

            globalLogger.addHandler(fileHandler);//Add the handler to the global logger

            fileHandler.setFormatter(new SimpleFormatter());//log in text, not xml

            //Set level to handler and logger
            fileHandler.setLevel(Level.FINE);
            globalLogger.setLevel(Level.FINE);

            globalLogger.config("Configuration done.");//Log that we are done setting up the logger

        }catch(IOException exception){//Something happened setting up file IO
            globalLogger.log(Level.SEVERE, "Error occured setting up FileHandler for logger.", exception);
        }

        this.eventBus.register(this);
        this.root = new MainWindowView(eventBus);
        /**
         * Any exceptions thrown by the UI will be caught here and an exception dialog will be displayed
         */
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            this.eventBus.post(new UnexpectedThrowableEvent(throwable, "The UI Thread threw an uncaught exception"));
        });

        root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

        stage.setTitle("GRIP Computer Vision Engine");
        stage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void triggerUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
        eventBus.post(event);
    }

    @Subscribe
    public final void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
        // Print throwable before showing the exception so that errors are in order in the console
        event.getThrowable().printStackTrace();
        PlatformImpl.runAndWait(() -> {
            synchronized (this.dialogLock) {
                try {
                    // Don't create more than one exception dialog at the same time
                    final ExceptionAlert exceptionAlert = new ExceptionAlert(root, event.getThrowable(), event.getMessage(), event.isFatal(), getHostServices());
                    exceptionAlert.setInitialFocus();
                    exceptionAlert.showAndWait();
                } catch (Throwable e) {
                    // Well in this case something has gone very, very wrong
                    // We don't want to create a feedback loop either.
                    e.printStackTrace();
                    System.exit(1); // Ensure we shut down the application if we get an exception
                }
            }
        });

        if(event.isFatal()) {
            System.err.println("Original fatal exception");
            event.getThrowable().printStackTrace();
            System.exit(1);
        }
    }
}
