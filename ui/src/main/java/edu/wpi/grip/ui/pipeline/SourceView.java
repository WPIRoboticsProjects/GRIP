package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.FatalErrorEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.events.SourceStartedEvent;
import edu.wpi.grip.core.events.SourceStoppedEvent;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * A JavaFX control that represents a {@link Source}.  <code>SourceView</code>s are somewhat analogous to
 * {@link StepView}s in thatthe pipeline contrains them and they contain some sockets, but <code>SourceView</code>s
 * only have output sockets, and they show up in a different place.
 */
public class SourceView extends VBox {

    private static final Image startImage = new Image(SourceView.class.getResourceAsStream("/edu/wpi/grip/ui/icons/start.png"));
    private static final Image stopImage = new Image(SourceView.class.getResourceAsStream("/edu/wpi/grip/ui/icons/stop.png"));

    @FXML
    private Label name;

    @FXML
    private VBox sockets;

    @FXML
    private ToggleButton startStopButton;

    @FXML
    private Tooltip startStopTooltip;

    private final EventBus eventBus;
    private final Source source;

    public SourceView(EventBus eventBus, Source source) {
        this.eventBus = eventBus;
        this.source = source;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Source.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        this.name.setText(source.getName());

        if (!source.isRestartable()) startStopButton.setVisible(false);
        else {
            HBox.setHgrow(startStopButton, Priority.NEVER);
            startStopButton.setContentDisplay(ContentDisplay.RIGHT);

            startStopButton.addEventFilter(MouseEvent.MOUSE_RELEASED, (event) -> {
                event.consume();
                if (!startStopButton.isSelected()) try {
                    source.start(eventBus);
                } catch (IOException e) {
                    eventBus.post(new FatalErrorEvent(e));
                }
                else try {
                    source.stop();
                } catch (Exception e) {
                    eventBus.post(new FatalErrorEvent(e));
                }
            });
            startStopButton.selectedProperty().addListener((o, oldV, newV) -> {
                final String stopCameraText = "Stop Camera";
                final String startCameraText = "Start Camera";
                final ImageView icon = newV ? new ImageView(stopImage) : new ImageView(startImage);
                icon.setFitHeight(DPIUtility.MINI_ICON_SIZE);
                icon.setFitWidth(DPIUtility.MINI_ICON_SIZE);
                startStopButton.setGraphic(icon);
                startStopTooltip.setText(newV ? stopCameraText : startCameraText);
                startStopButton.setAccessibleText(newV ? stopCameraText : startCameraText);
            });
            startStopButton.setSelected(source.isRunning());
        }

        for (OutputSocket<?> socket : source.getOutputSockets()) {
            this.sockets.getChildren().add(new OutputSocketView(eventBus, socket));
        }

        eventBus.register(this);
    }

    public Source getSource() {
        return this.source;
    }

    /**
     * @return An unmodifiable list of {@link OutputSocketView}s corresponding to the sockets that this source produces
     */
    @SuppressWarnings("unchecked")
    public ObservableList<OutputSocketView> getOutputSockets() {
        return (ObservableList) this.sockets.getChildrenUnmodifiable();
    }

    @FXML
    public void delete() {
        this.eventBus.post(new SourceRemovedEvent(this.getSource()));
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
