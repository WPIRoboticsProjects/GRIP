package edu.wpi.grip.ui;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays an alert with an exception and provides a place for a user to supply information about what caused the error.
 * Includes a textbox with formatted markdown to allow the issue to be pasted into GitHub easily.
 * Also, provides links with quick access to the githup issue page.
 */
public final class ExceptionView extends Alert {
    private static final String PROJECT_ISSUE_LINK = "https://github.com/WPIRoboticsProjects/GRIP/issues";
    private static final String ISSUE_PROMPT_QUESTION = "What were the actions that caused this error occurred?";

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

    private final ButtonType copyToClipboardBtnType = new ButtonType("Copy To Clipboard");
    private final ButtonType openGitHubIssuesBtnType = new ButtonType("Open GitHub Issues");

    /**
     * @param throwable The throwable exception to display this alert for.
     * @param services  The host services, allows access to {@link HostServices#showDocument(String)} in order to display
     *                  the issue website.
     * @see <a href="http://code.makery.ch/blog/javafx-dialogs-official/">Inspiration</a>
     */
    public ExceptionView(final Throwable throwable, final HostServices services) {
        super(AlertType.ERROR);
        checkNotNull(throwable, "The Throwable can not be null");
        checkNotNull(services, "HostServices can not be null");

        this.exceptionMessage = generateExceptionMessage(throwable);
        this.systemInfoMessage = generateSystemInfoMessage();
        this.initialCause = generateInitialCause(throwable);

        this.setTitle(initialCause.getClass().getSimpleName());
        this.setHeaderText(initialCause.getMessage());

        this.getButtonTypes().addAll(copyToClipboardBtnType, openGitHubIssuesBtnType);

        final Label whatHappenedLabel = new Label(ISSUE_PROMPT_QUESTION);

        final TextArea issueText = new TextArea(issueText(""));

        final TextArea inputBox = new TextArea();
        inputBox.textProperty().addListener((observable, oldValue, newValue) -> issueText.setText(issueText(newValue)));
        inputBox.setPromptText("Short description of what you were doing when this dialog appeared.");
        inputBox.setEditable(true);
        inputBox.setWrapText(true);
        inputBox.setMaxWidth(Double.MAX_VALUE);
        inputBox.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(inputBox, Priority.ALWAYS);
        GridPane.setHgrow(inputBox, Priority.ALWAYS);

        final GridPane messageContent = new GridPane();
        messageContent.setMaxWidth(Double.MAX_VALUE);
        messageContent.setMaxHeight(Double.MAX_VALUE);
        messageContent.add(whatHappenedLabel, 0, 0);
        messageContent.add(inputBox, 0, 1);
        this.getDialogPane().setContent(messageContent);

        final Label issuePasteLabel = new Label("Please paste this into an issue on the project GitHub:");

        issueText.setEditable(false);
        issueText.setWrapText(true);

        issueText.setMaxWidth(Double.MAX_VALUE);
        issueText.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(issueText, Priority.ALWAYS);
        GridPane.setHgrow(issueText, Priority.ALWAYS);

        final GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(issuePasteLabel, 0, 0);
        expContent.add(issueText, 0, 1);

        // Set expandable Exception into the dialog pane.
        this.getDialogPane().setExpandableContent(expContent);
        this.getDialogPane().setExpanded(true);


        // Prevent these two buttons from causing the alert to close
        final Button copyToClipboardBtn = (Button) this.getDialogPane().lookupButton(copyToClipboardBtnType);
        copyToClipboardBtn.addEventFilter(ActionEvent.ACTION, event -> {
            final ClipboardContent content = new ClipboardContent();
            content.putString(issueText.getText());
            Clipboard.getSystemClipboard().setContent(content);
            // Prevent the dialog from closing
            event.consume();
        });

        final Button openGitHubIssueBtn = (Button) this.getDialogPane().lookupButton(openGitHubIssuesBtnType);
        openGitHubIssueBtn.addEventFilter(ActionEvent.ACTION, event -> {
            services.showDocument(PROJECT_ISSUE_LINK);
            // Prevent the dialog from closing
            event.consume();
        });

        // Set the initial focus to the input box so the cursor goes there first
        Platform.runLater(() -> inputBox.requestFocus());
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
     * Takes the throwable and generates the markdown for the exception.
     *
     * @param throwable The initial throwable passed to the constructor
     * @return The markdown for the exception.
     */
    private String generateExceptionMessage(Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        return new StringBuilder(sw.toString()
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
     * @param message The message from the input box
     * @return The fully constructed issue text.
     */
    private String issueText(String message) {
        return new StringBuilder(ISSUE_PROMPT_QUESTION)
                .append("\n\n")
                .append(message)
                .append("\n\n")
                .append(systemInfoMessage)
                .append(exceptionMessage).toString();
    }


}
