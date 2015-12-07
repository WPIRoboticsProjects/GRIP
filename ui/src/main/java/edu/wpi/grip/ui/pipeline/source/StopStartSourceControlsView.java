package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.StopStartSource;
import edu.wpi.grip.core.events.FatalErrorEvent;
import edu.wpi.grip.core.events.SourceStartedEvent;
import edu.wpi.grip.core.events.SourceStoppedEvent;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls for a {@link StopStartSource}
 */
public class StopStartSourceControlsView extends SourceControlsView<StopStartSource> {

    private static final Image startImage = new Image(StopStartSourceControlsView.class.getResourceAsStream("/edu/wpi/grip/ui/icons/start.png"));
    private static final Image stopImage = new Image(StopStartSourceControlsView.class.getResourceAsStream("/edu/wpi/grip/ui/icons/stop.png"));

    private final ToggleButton startStopButton;

    private final Tooltip startStopTooltip;

    private final StopStartSource source;

    public StopStartSourceControlsView(final EventBus eventBus, final StopStartSource source) {
        this.source = checkNotNull(source, "Source can not be null");
        startStopButton = new ToggleButton(null, getGraphic());
        startStopTooltip = new Tooltip(getButtonActionString());
        startStopButton.setAccessibleText(getButtonActionString());
        startStopButton.setTooltip(startStopTooltip);
        startStopButton.setContentDisplay(ContentDisplay.RIGHT);
        startStopButton.setSelected(source.isRunning());
        startStopButton.addEventFilter(MouseEvent.MOUSE_RELEASED, (event) -> {
            event.consume();
            if (!startStopButton.isSelected()) try {
                source.start(eventBus);
                // If this fails then an SourceStartedEvent will not be posted
            } catch (IOException e) {
                eventBus.post(new FatalErrorEvent(e));
            }
            else try {
                source.stop();
                // If this fails then an SourceStoppedEvent will not be posted
            } catch (TimeoutException | IOException e) {
                eventBus.post(new FatalErrorEvent(e));
            }
        });

        startStopButton.selectedProperty().addListener((o, oldV, newV) -> {
            startStopTooltip.setText(getButtonActionString());
            startStopButton.setAccessibleText(getButtonActionString());
            startStopButton.setGraphic(getGraphic());
        });
        HBox.setHgrow(startStopButton, Priority.NEVER);
        this.getChildren().add(startStopButton);

        eventBus.register(this);
    }

    /**
     * Gets the graphic that should be used for the button given the current source's state
     * @return The graphic to show on the button.
     */
    private ImageView getGraphic() {
        final ImageView icon = source.isRunning() ? new ImageView(stopImage) : new ImageView(startImage);
        icon.setFitHeight(DPIUtility.MINI_ICON_SIZE);
        icon.setFitWidth(DPIUtility.MINI_ICON_SIZE);
        return icon;
    }

    /**
     * @return The description of what action clicking the button will have.
     */
    private String getButtonActionString() {
        return (source.isRunning() ? "Stop" : "Start" ) + " Source";
    }


    @Subscribe
    public void onSourceStarted(SourceStartedEvent event) {
        if (source == event.getSource()) {
            Platform.runLater(() -> {
                startStopButton.setSelected(true);
            });
        }
    }

    @Subscribe
    public void onSourceStopped(SourceStoppedEvent event) {
        if (source == event.getSource()) {
            Platform.runLater(() -> {
                startStopButton.setSelected(false);
            });
        }
    }

}
