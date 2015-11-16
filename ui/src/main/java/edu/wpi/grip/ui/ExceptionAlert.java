package edu.wpi.grip.ui;

import com.google.common.net.UrlEscapers;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;

import java.io.PrintWriter;
import java.io.StringWriter;

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
    private final Throwable initialCause;

    private final ButtonType openGitHubIssuesBtnType = new ButtonType("Open GitHub Issues");
    private final ButtonType copyToClipboardBtnType = new ButtonType("Copy To Clipboard");
    private final ButtonType closeBtnType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    /**
     * @param throwable The throwable exception to display this alert for.
     * @param services  The host services, allows access to {@link HostServices#showDocument(String)} in order to display
     *                  the issue website.
     * @see <a href="http://code.makery.ch/blog/javafx-dialogs-official/">Inspiration</a>
     */
    public ExceptionAlert(final Parent root, final Throwable throwable, final HostServices services) {
        super(AlertType.ERROR);
        checkNotNull(throwable, "The Throwable can not be null");
        checkNotNull(services, "HostServices can not be null");

        this.exceptionMessage = generateExceptionMessage(throwable);
        this.systemInfoMessage = generateSystemInfoMessage();
        this.initialCause = generateInitialCause(throwable);

        this.setTitle(initialCause.getClass().getSimpleName());
        this.setHeaderText(initialCause.getMessage());

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

        final TextArea issueText = new TextArea(stackTrace(throwable));
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
        Platform.runLater(() -> openGitHubIssueBtn.requestFocus());
    }

    /**
     * Iterates through the causes until the root cause is found.
     *
     * @param throwable The throwable to iterate through.
     * @return The initial throwable
     */
    private Throwable generateInitialCause(Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        } else {
            return generateInitialCause(throwable.getCause());
        }
    }

    /**
     * Generates the Throwable's stack trace as a string.
     */
    private String stackTrace(Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Takes the throwable and generates the markdown for the exception.
     *
     * @param throwable The initial throwable passed to the constructor
     * @return The markdown for the exception.
     */
    private String generateExceptionMessage(Throwable throwable) {
        return new StringBuilder(stackTrace(throwable)
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
        return new StringBuilder(ISSUE_PROMPT_QUESTION)
                .append("\n\n\n\n")
                .append(systemInfoMessage)
                .append(exceptionMessage).toString();
    }


}
