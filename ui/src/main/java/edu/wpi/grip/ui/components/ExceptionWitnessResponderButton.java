package edu.wpi.grip.ui.components;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.commons.lang3.text.WordUtils;
import org.controlsfx.control.PopOver;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens and responds to events posted by the {@link edu.wpi.grip.core.util.ExceptionWitness}
 */
public final class ExceptionWitnessResponderButton extends Button {
    @VisibleForTesting
    @SuppressWarnings({"PMD.DefaultPackage", "PMD.FieldDeclarationsShouldBeAtStartOfClass"})
    static final String STYLE_CLASS = "exception-witness-responder-button";

    private final Object origin;
    private final Tooltip tooltip;
    private final String popOverTitle;
    /**
     * Do not use, instead call {@link #getPopover()}
     */
    private volatile Optional<ExceptionPopOver> popOver = Optional.empty();

    public interface Factory {
        ExceptionWitnessResponderButton create(Object origin, String popOverTitle);
    }

    protected static class ExceptionPopOver extends PopOver {
        @VisibleForTesting
        @SuppressWarnings({"PMD.DefaultPackage", "PMD.FieldDeclarationsShouldBeAtStartOfClass"})
        static final String STYLE_CLASS = "error-pop-over";

        private final Text errorMessage = new Text();
        private final TextArea stackTrace = new TextArea();
        private final TitledPane stackTracePane;

        private ExceptionPopOver(String title) {
            super();

            setTitle(title);
            stackTrace.setEditable(false);

            getStyleClass().add(this.STYLE_CLASS);
            setHeaderAlwaysVisible(true);
            setDetachable(false);

            GridPane.setHalignment(errorMessage, HPos.CENTER);
            GridPane.setValignment(errorMessage, VPos.CENTER);
            GridPane.setHgrow(errorMessage, Priority.ALWAYS);
            GridPane.setMargin(errorMessage, new Insets(5, 5, 5, 5));
            errorMessage.setTextAlignment(TextAlignment.CENTER);


            stackTracePane = new TitledPane("Stack Trace", stackTrace);
            stackTracePane.managedProperty().bind(stackTracePane.visibleProperty());
            stackTracePane.setTextFill(Color.GRAY);
            stackTracePane.setCollapsible(true);
            stackTracePane.setExpanded(false);
            final Accordion accordion = new Accordion(stackTracePane);

            GridPane.setVgrow(accordion, Priority.ALWAYS);
            GridPane.setHgrow(accordion, Priority.ALWAYS);

            final GridPane expContent = new GridPane();
            expContent.setVgap(10);
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(errorMessage, 0, 0);
            expContent.add(accordion, 0, 1);

            setContentNode(expContent);
        }

        /**
         * Assigns the contents of the popover using the data from the exception event.
         *
         * @param event The event that this popover should display.
         */
        private void assignFromExceptionEvent(ExceptionEvent event) {
            final int wrapNumber = 80;
            final String errorMessageText =
                    WordUtils.wrap(event.getMessage(), wrapNumber, null, true);
            errorMessage.setText(errorMessageText);
            if (event.getException().isPresent()) {
                final Exception exception = event.getException().get();
                final String exceptionMessage = Throwables.getStackTraceAsString(exception);
                // Otherwise it is impossible to scroll the text field because it is updated so frequently
                if (!exceptionMessage.equals(stackTrace.getText())) {
                    stackTrace.setText(exceptionMessage);
                }

                stackTracePane.setVisible(true);
            } else {
                stackTracePane.setVisible(false);
                stackTrace.setText("");
            }
        }

    }

    /**
     * @param origin The same origin that is passed to the {@link edu.wpi.grip.core.util.ExceptionWitness}
     */
    @Inject
    ExceptionWitnessResponderButton(@Assisted Object origin, @Assisted String popOverTitle) {
        super();

        this.origin = checkNotNull(origin, "The origin can not be null");
        this.popOverTitle = checkNotNull(popOverTitle, "The pop over title can not be null");
        this.tooltip = new Tooltip();
        this.getStyleClass().add(STYLE_CLASS);

        ImageView icon = new ImageView("/edu/wpi/grip/ui/icons/warning.png");

        setOnMouseClicked(event -> getPopover().show(this));
        setVisible(false);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(icon);
        icon.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
        icon.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

        FadeTransition ft = new FadeTransition(Duration.millis(750), icon);
        ft.setToValue(0.1);
        ft.setCycleCount(Transition.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();
    }

    /**
     * If the popover hasn't been created before then this creates the popover.
     * Otherwise it returns the popover that was constructed the first time this method was run.
     *
     * @return The popover for this button
     */
    private synchronized ExceptionPopOver getPopover() {
        if (!popOver.isPresent()) {
            final ExceptionPopOver newPopOver = new ExceptionPopOver(this.popOverTitle);
            // Scene and root are null when the the constructor is called
            // They are not null once they are added to a scene.
            assert getScene() != null : "Scene should not be null, this object has not been added to the scene yet";
            assert getScene().getRoot() != null : "Root should not be null, this object has not been added to the scene yet";
            final Parent root = getScene().getRoot();
            newPopOver.getRoot().getStylesheets().addAll(root.getStylesheets());
            newPopOver.getRoot().setStyle(root.getStyle());
            popOver = Optional.of(newPopOver);
        }
        return popOver.get();
    }

    @Subscribe
    public void onExceptionEvent(ExceptionEvent event) {
        if (getScene() == null || getScene().getRoot() == null) {
            // We are responding to an event prior to this button being added to the scene. Ignore this for now.
            return;
        }
        if (event.getOrigin().equals(origin)) {
            // Not timing sensitive. Can remain Platform.runLater
            Platform.runLater(() -> {
                // Update the text of the tooltip to this event
                tooltip.setText(WordUtils.wrap(event.getMessage(), 90, null, true));

                getPopover().assignFromExceptionEvent(event);
                this.setVisible(true);
            });
        }
    }

    @Subscribe
    public void onExceptionClearedEvent(ExceptionClearedEvent event) {
        if (getScene() == null || getScene().getRoot() == null) {
            // We are responding to an event prior to this button being added to the scene. Ignore this for now.
            return;
        }
        if (event.getOrigin().equals(origin)) {
            // Not timing sensitive. Can remain Platform.runLater
            Platform.runLater(() -> {
                getPopover().hide();
                setVisible(false);
            });
        }
    }
}
