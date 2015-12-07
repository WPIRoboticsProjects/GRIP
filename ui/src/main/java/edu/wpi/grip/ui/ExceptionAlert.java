package edu.wpi.grip.ui;

import com.google.common.base.Throwables;
import com.google.common.net.UrlEscapers;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays an alert with an exception and provides a place for a user to supply information about what caused the error.
 * Includes a textbox with formatted markdown to allow the issue to be pasted into GitHub easily.
 * Also, provides links with quick access to the githup issue page.
 */
public final class ExceptionAlert extends Alert {
    private static final String
            PROJECT_ISSUE_LINK = "https://github.com/WPIRoboticsProjects/GRIP/issues/new",
            ISSUE_PROMPT_QUESTION = "What were the actions performed prior to this error appearing?",
            ISSUE_PROMPT_TEXT = "An exception occurred!  Please open an issue on the project GitHub.\n"
                    + "We value your feedback and want to hear about the problems you encounter.";

    private static final String systemOptions[] = {
            "javafx.version",
            "java.runtime.name",
            "java.vm.version",
            "java.vm.vendor",
            "java.vm.name",
            "java.runtime.version",
            "java.awt.graphicsenv",
            "javafx.runtime.version",
            "os.name",
            "os.version",
            "os.arch",
            "file.encoding",
            "java.vm.info",
            "java.version",
            "sun.arch.data.model",
            "sun.cpu.endian"
    };

    private final String exceptionMessage;
    private final String systemInfoMessage;
    private final String additionalInfoMessage;
    private final String message;
    private final Throwable initialCause;

    private final ButtonType openGitHubIssuesBtnType = new ButtonType("Open GitHub Issues");
    private final ButtonType copyToClipboardBtnType = new ButtonType("Copy To Clipboard");
    private final Node initialFocusElement;

    /**
     * @param throwable The throwable exception to display this alert for.
     * @param services  The host services, allows access to {@link HostServices#showDocument(String)} in order to display
     *                  the issue website.
     * @see <a href="http://code.makery.ch/blog/javafx-dialogs-official/">Inspiration</a>
     */
    public ExceptionAlert(final Parent root, final Throwable throwable, final String message, boolean isFatal, final HostServices services) {
        super(AlertType.ERROR);
        checkNotNull(root, "The parent can not be null");
        checkNotNull(throwable, "The Throwable can not be null");
        this.message = checkNotNull(message, "The message can not be null");
        checkNotNull(services, "HostServices can not be null");

        final ButtonType closeBtnType = new ButtonType(isFatal ? "Quit" : "Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        this.exceptionMessage = generateExceptionMessage(throwable);
        this.systemInfoMessage = generateSystemInfoMessage();
        this.additionalInfoMessage = generateAdditionalInfoMessage();
        this.initialCause = getInitialCause(throwable);

        this.setTitle(initialCause.getClass().getSimpleName());
        this.setHeaderText((isFatal ? "FATAL: " : "") + message);

        // Set stylesheet
        this.getDialogPane().styleProperty().bind(root.styleProperty());
        this.getDialogPane().getStylesheets().addAll(root.getStylesheets());

        // Add two additional buttons
        this.getButtonTypes().removeIf((buttonType) -> buttonType.equals(ButtonType.OK));
        this.getButtonTypes().addAll(openGitHubIssuesBtnType, copyToClipboardBtnType, closeBtnType);


        final GridPane dialogContent = new GridPane();
        dialogContent.setMaxWidth(Double.MAX_VALUE);
        dialogContent.setMaxHeight(Double.MAX_VALUE);


        final Label issuePasteLabel = new Label(ISSUE_PROMPT_TEXT);
        issuePasteLabel.setWrapText(true);

        final TextArea issueText = new TextArea(Throwables.getStackTraceAsString(throwable));
        issuePasteLabel.setLabelFor(issueText);
        issueText.setEditable(false);
        issueText.setWrapText(true);

        issueText.setMaxWidth(Double.MAX_VALUE);
        issueText.setMaxHeight(Double.MAX_VALUE);

        dialogContent.add(issuePasteLabel, 0, 0);
        dialogContent.add(issueText, 0, 1);
        this.getDialogPane().setContent(dialogContent);


        // Prevent these two buttons from causing the alert to close
        final Button copyToClipboardBtn = (Button) this.getDialogPane().lookupButton(copyToClipboardBtnType);
        copyToClipboardBtn.addEventFilter(ActionEvent.ACTION, event -> {
            final ClipboardContent content = new ClipboardContent();
            content.putString(issueText());
            Clipboard.getSystemClipboard().setContent(content);
            // Prevent the dialog from closing
            event.consume();
        });


        final Button openGitHubIssueBtn = (Button) this.getDialogPane().lookupButton(openGitHubIssuesBtnType);
        openGitHubIssueBtn.addEventFilter(ActionEvent.ACTION, event -> {
            final StringBuilder URL_STRING = new StringBuilder(PROJECT_ISSUE_LINK)
                    .append("?title=")
                    .append(
                            UrlEscapers.urlFormParameterEscaper().escape(
                                    initialCause.getClass().getSimpleName() + ": " + initialCause.getMessage()
                            )
                    ).append("&body=")
                    .append(UrlEscapers.urlFormParameterEscaper().escape(
                            issueText()
                    ));


            services.showDocument(URL_STRING.toString());
            // Prevent the dialog from closing
            event.consume();
        });

        // Set the initial focus to the input box so the cursor goes there first
        this.initialFocusElement = openGitHubIssueBtn;
    }

    /**
     * Call this to assign the initial focus element.
     * This is not done in the constructor so that the object is not exposed to any other threads from within the objects constructor
     */
    public final void setInitialFocus() {
        Platform.runLater(() -> initialFocusElement.requestFocus());
    }

    /**
     * Iterates through the causes until the root cause is found.
     *
     * @param throwable The throwable to iterate through.
     * @return The initial throwable
     */
    private Throwable getInitialCause(Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        } else {
            return getInitialCause(throwable.getCause());
        }
    }

    private String generateAdditionalInfoMessage() {
        return "Message: " + message + "\n";
    }

    /**
     * Takes the throwable and generates the markdown for the exception.
     *
     * @param throwable The initial throwable passed to the constructor
     * @return The markdown for the exception.
     */
    private String generateExceptionMessage(Throwable throwable) {
        return new StringBuilder(Throwables.getStackTraceAsString(throwable)
                /* Allow users to maintain anonymity */
                .replace(System.getProperty("user.home"), "$HOME").replace(System.getProperty("user.name"), "$USER"))
                .insert(0, "## Stack Trace:\n```java\n").append("\n```").toString();
    }

    /**
     * Takes the options listed above and creates a markdown table with each param.
     *
     * @return The markdown text for the System info.
     */
    private String generateSystemInfoMessage() {
        final StringBuilder systemInfo = new StringBuilder("## System Info:\n\n");
        systemInfo.append("Property Name | Property \n ----- | -----\n");
        for (String option : systemOptions) {
            systemInfo.append(option).append(" | ").append(System.getProperty(option)).append("\n");
        }
        return systemInfo.append("\n").toString();
    }

    /**
     * Creates the text that gets pasted into the new github issue.
     *
     * @return The fully constructed issue text.
     */
    private String issueText() {
        return ISSUE_PROMPT_QUESTION
                + "\n\n\n\n"
                + additionalInfoMessage
                + "\n"
                + systemInfoMessage
                + exceptionMessage;
    }


}
