package edu.wpi.grip.ui.components;


import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.util.service.RestartableService;
import edu.wpi.grip.core.util.service.SingleActionListener;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StartStoppableButton extends ToggleButton {
    protected static final String BASE_STYLE_CLASS = "start-stoppable-button";
    protected static final String STARTED_STYLE_CLASS = "started";
    protected static final String STOPPED_STYLE_CLASS = "stopped";

    private static final Image startImage = new Image(StartStoppableButton.class.getResourceAsStream("/edu/wpi/grip/ui/icons/start.png"));
    private static final Image stopImage = new Image(StartStoppableButton.class.getResourceAsStream("/edu/wpi/grip/ui/icons/stop.png"));

    private final Tooltip startStopTooltip;

    private final RestartableService service;

    public interface Factory {
        StartStoppableButton create(RestartableService startStoppable);
    }

    @Inject
    StartStoppableButton(@Assisted final RestartableService service) {
        super(null, pickGraphic(service));
        this.service = checkNotNull(service, "RestartableService can not be null");
        this.startStopTooltip = new Tooltip(getButtonActionString());
        setContentDisplay(ContentDisplay.RIGHT);

        // Run the listeners on the javafx thread
        service.addListener(new SingleActionListener(() -> assignState()), Platform::runLater);
        assignState();
        setAllFromState();

        addEventFilter(MouseEvent.MOUSE_RELEASED, (event) -> {
            setDisable(true);
            event.consume();
            if (!isSelected()) {
                this.service.startAsync();
            } else {
                this.service.stopAsync();
            }
        });

        selectedProperty().addListener((o, oldV, newV) -> {
            setAllFromState();
        });
        HBox.setHgrow(this, Priority.NEVER);
    }

    private void setAllFromState() {
        assert Platform.isFxApplicationThread() : "This must be called from the FX Thread";
        startStopTooltip.setText(getButtonActionString());
        setAccessibleText(getButtonActionString());
        setGraphic(pickGraphic(service));
        getStyleClass().addAll(getCurrentStyleClasses());
    }

    /**
     * @return The description of what action clicking the button will have.
     */
    private String getButtonActionString() {
        return CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(service.state().toString());
    }

    /**
     * @return The style classes that should be assigned to the button
     */
    private List<String> getCurrentStyleClasses() {
        return Arrays.asList(BASE_STYLE_CLASS, service.isRunning() ? STARTED_STYLE_CLASS : STOPPED_STYLE_CLASS);
    }

    /**
     * Assigns the state of the button from the {@link RestartableService}
     */
    private void assignState() {
        setSelected(service.isRunning());
        final Service.State state = service.state();
        setDisable(state.equals(Service.State.STARTING) || state.equals(Service.State.STOPPING));
    }

    /**
     * Gets the graphic that should be used for the button given the current source's state
     *
     * @return The graphic to show on the button.
     */
    private static ImageView pickGraphic(RestartableService startStoppable) {
        final boolean running = startStoppable.isRunning();
        final ImageView icon = running ? new ImageView(stopImage) : new ImageView(startImage);
        if (!running) {
            // If we are not running then we want the icon to flash
            final FadeTransition ft = new FadeTransition(Duration.millis(750), icon);
            ft.setToValue(0.1);
            ft.setCycleCount(Transition.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();
        }
        icon.setFitHeight(DPIUtility.MINI_ICON_SIZE);
        icon.setFitWidth(DPIUtility.MINI_ICON_SIZE);
        return icon;
    }

}
