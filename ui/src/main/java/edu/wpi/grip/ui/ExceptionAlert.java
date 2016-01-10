package edu.wpi.grip.ui;

import com.google.common.base.Throwables;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays an alert with an exception and provides a place for a user to supply information about what caused the error.
 * Includes a textbox with formatted markdown to allow the issue to be pasted into GitHub easily.
 * Also, provides links with quick access to the githup issue page.
 */
public final class ExceptionAlert extends Alert {
    private static final String PROJECT_ISSUE_LINK = "https://github.com/WPIRoboticsProjects/GRIP/issues/new?body=Ple" +
            "ase%20describe%20what%20actions%20we%20can%20take%20to%20reproduce%20the%20bug%20you%20found%2C%20includ" +
            "ing%20the%20error%20message.";

    private static final String ISSUE_PROMPT_TEXT = "An exception occurred!  Please open an issue on the project " +
            "GitHub.\nWe value your feedback and want to hear about the problems you encounter.";

    private static final String systemProperties[] = {
            "java.version", "javafx.version", "os.name", "os.version", "os.arch",
    };

    private static final ButtonType COPY = new ButtonType("Copy to Clipboard");
    private static final ButtonType REPORT = new ButtonType("Report Issue");

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
        checkNotNull(services, "HostServices can not be null");

        final ButtonType closeBtnType = new ButtonType(isFatal ? "Quit" : "Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        String exceptionMessage = generateExceptionMessage(throwable);
        String systemInfoMessage = generateSystemInfoMessage();

        this.setTitle(getInitialCause(throwable).getClass().getSimpleName());
        this.setHeaderText((isFatal ? "FATAL: " : "") + message);

        // Set stylesheet
        this.getDialogPane().styleProperty().bind(root.styleProperty());
        this.getDialogPane().getStylesheets().addAll(root.getStylesheets());

        this.setResizable(true);

        // Add two additional buttons
        this.getButtonTypes().setAll(COPY, closeBtnType, REPORT);


        final GridPane dialogContent = new GridPane();
        dialogContent.getStyleClass().add("exception-pane");
        dialogContent.setMaxWidth(Double.MAX_VALUE);
        dialogContent.setMaxHeight(Double.MAX_VALUE);


        final Label issuePasteLabel = new Label(ISSUE_PROMPT_TEXT);
        issuePasteLabel.setWrapText(true);

        final TextArea issueText = new TextArea(message + "\n" + exceptionMessage + "\n" + systemInfoMessage);
        issueText.getStyleClass().add("exception-text");
        issuePasteLabel.setLabelFor(issueText);
        issueText.setEditable(false);

        issueText.setMaxWidth(Double.MAX_VALUE);
        issueText.setMaxHeight(Double.MAX_VALUE);

        dialogContent.add(issuePasteLabel, 0, 0);
        dialogContent.add(issueText, 0, 1);
        GridPane.setHgrow(issueText, Priority.ALWAYS);
        GridPane.setVgrow(issueText, Priority.ALWAYS);
        this.getDialogPane().setContent(dialogContent);

        final Button copyBtn = (Button) this.getDialogPane().lookupButton(COPY);
        copyBtn.addEventFilter(ActionEvent.ACTION, event -> {
            final ClipboardContent content = new ClipboardContent();
            content.putString(issueText.getText());
            Clipboard.getSystemClipboard().setContent(content);
            issueText.requestFocus();
            issueText.selectAll();
            event.consume(); // Prevent the dialog from closing
        });

        final Button openGitHubIssueBtn = (Button) this.getDialogPane().lookupButton(REPORT);
        openGitHubIssueBtn.addEventFilter(ActionEvent.ACTION, event -> {
            services.showDocument(PROJECT_ISSUE_LINK);
            event.consume(); // Prevent the dialog from closing
        });

        // Set the initial focus to the input box so the cursor goes there first
        this.initialFocusElement = openGitHubIssueBtn;
    }

    /**
     * Call this to assign the initial focus element.
     * This is not done in the constructor so that the object is not exposed to any other threads from within the objects constructor
     */
    public final void setInitialFocus() {
        Platform.runLater(initialFocusElement::requestFocus);
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
        systemInfo.append("GRIP Version | " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion() + "\n");
        for (String option : systemProperties) {
            systemInfo.append(option).append(" | ").append(System.getProperty(option)).append("\n");
        }
        return systemInfo.append("\n").toString();
    }
}
