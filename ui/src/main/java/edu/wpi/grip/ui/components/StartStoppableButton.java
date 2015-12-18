package edu.wpi.grip.ui.components;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.StartStoppable;
import edu.wpi.grip.core.events.StartedStoppedEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StartStoppableButton extends ToggleButton {
    protected static final String BASE_STYLE_CLASS = "start-stoppable-button";
    protected static final String STARTED_STYLE_CLASS = "started";
    protected static final String STOPPED_STYLE_CLASS = "stopped";

    private static final Image startImage = new Image(StartStoppableButton.class.getResourceAsStream("/edu/wpi/grip/ui/icons/start.png"));
    private static final Image stopImage = new Image(StartStoppableButton.class.getResourceAsStream("/edu/wpi/grip/ui/icons/stop.png"));

    private final Tooltip startStopTooltip;

    private final StartStoppable startStoppable;

    public StartStoppableButton(final EventBus eventBus, final StartStoppable startStoppable) {
        super(null, pickGraphic(startStoppable));
        this.startStoppable = checkNotNull(startStoppable, "StartStoppable can not be null");
        this.startStopTooltip = new Tooltip(getButtonActionString());
        setContentDisplay(ContentDisplay.RIGHT);
        assignState();
        setAllFromState();

        addEventFilter(MouseEvent.MOUSE_RELEASED, (event) -> {
            event.consume();
            if (!isSelected()) try {
                startStoppable.start(eventBus);
                // If this fails then an StartedStoppedEvent will not be posted
            } catch (IOException e) {
                eventBus.post(new UnexpectedThrowableEvent(e, "Failed to start"));
            }
            else try {
                startStoppable.stop();
                // If this fails then an StartedStoppedEvent will not be posted
            } catch (TimeoutException | IOException e) {
                eventBus.post(new UnexpectedThrowableEvent(e, "Failed to stop"));
            }
        });

        selectedProperty().addListener((o, oldV, newV) -> {
            setAllFromState();
        });
        HBox.setHgrow(this, Priority.NEVER);

        eventBus.register(this);
    }

    private void setAllFromState() {
        assert Platform.isFxApplicationThread() : "This must be called from the FX Thread";
        startStopTooltip.setText(getButtonActionString());
        setAccessibleText(getButtonActionString());
        setGraphic(pickGraphic(startStoppable));
        getStyleClass().addAll(getCurrentStyleClasses());
    }

    /**
     * @return The description of what action clicking the button will have.
     */
    private String getButtonActionString() {
        return (startStoppable.isStarted() ? "Stop" : "Start");
    }

    /**
     * @return The style classes that should be assigned to the button
     */
    private List<String> getCurrentStyleClasses() {
        return Arrays.asList(BASE_STYLE_CLASS, startStoppable.isStarted() ? STARTED_STYLE_CLASS : STOPPED_STYLE_CLASS);
    }

    /**
     * Assigns the state of the button from the {@link StartStoppable}
     */
    private void assignState() {
        setSelected(startStoppable.isStarted());
    }


    @Subscribe
    public void onStartedStopped(StartedStoppedEvent event) {
        if (startStoppable.equals(event.getStartStoppable())) {
            Platform.runLater(this::assignState);
        }
    }

    /**
     * Gets the graphic that should be used for the button given the current source's state
     *
     * @return The graphic to show on the button.
     */
    private static ImageView pickGraphic(StartStoppable startStoppable) {
        final ImageView icon = startStoppable.isStarted() ? new ImageView(stopImage) : new ImageView(startImage);
        icon.setFitHeight(DPIUtility.MINI_ICON_SIZE);
        icon.setFitWidth(DPIUtility.MINI_ICON_SIZE);
        return icon;
    }

}
