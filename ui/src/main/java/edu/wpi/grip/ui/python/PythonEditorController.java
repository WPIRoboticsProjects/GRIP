package edu.wpi.grip.ui.python;

import edu.wpi.grip.core.operations.python.PythonOperationUtils;
import edu.wpi.grip.core.operations.python.PythonScriptFile;
import edu.wpi.grip.ui.annotations.ParametrizedController;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.python.core.PyException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controller for the python editor.
 */
@ParametrizedController(url = "PythonEditor.fxml")
public class PythonEditorController {

  private static final Logger logger = Logger.getLogger(PythonEditorController.class.getName());

  private Window window;
  @FXML private BorderPane root;
  private final CodeArea codeArea = new CodeArea();
  private File scriptFile = null;
  private Predicate<String> operationNameTaken;
  private HostServices hostServices;

  /**
   * Array of python keywords.
   */
  private static final String[] KEYWORDS = new String[] {
      "and", "as", "assert", "break", "class", "continue",
      "def", "del", "elif", "else", "except", "exec",
      "finally", "for", "from", "global", "if", "import",
      "in", "is", "lambda", "not", "or", "pass", "print",
      "raise", "return", "try", "while", "with", "yield"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "#[^\n]*|'''(.|\\R)*?'''";

  /**
   * Regular expression pattern matching text to be styled differently.
   */
  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );

  @FXML
  private void initialize() {
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    codeArea.richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(change -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
    codeArea.replaceText(0, 0, PythonScriptFile.TEMPLATE);
    codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 10pt;");
    codeArea.getStylesheets()
        .add(getClass().getResource("python-keywords.css").toExternalForm());
    root.setCenter(codeArea);
  }

  /**
   * Computes highlighting for python code.
   *
   * @param text the text to highlight
   */
  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("PAREN") != null ? "paren" :
                  matcher.group("BRACE") != null ? "brace" :
                      matcher.group("BRACKET") != null ? "bracket" :
                          matcher.group("STRING") != null ? "string" :
                              matcher.group("COMMENT") != null ? "comment" :
                                  null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  /**
   * Injects members required for this controller.
   *
   * @param operationNameTaken predicate testing if an operation with a given name is in the palette
   *                           or pipeline
   */
  public void injectMembers(Predicate<String> operationNameTaken, HostServices hostServices) {
    this.operationNameTaken = checkNotNull(operationNameTaken, "operationNameTaken");
    this.hostServices = checkNotNull(hostServices, "hostServices");
  }

  /**
   * Gets the window displaying the editor.
   */
  private Window getWindow() {
    if (window == null) {
      window = root.getScene().getWindow();
    }
    return window;
  }

  /**
   * Gets the root {@code BorderPane} for the editor.
   */
  public @Nonnull BorderPane getRoot() {
    return root;
  }

  /**
   * Gets the script text in the editor. Returns null if no name is present, the name is already
   * used for another operation, or if there is an error in the script.
   */
  public @Nullable String getScript() {
    if (checkName()) {
      return null;
    }
    String script = codeArea.getText();
    try {
      PythonScriptFile.create(script);
      return script;
    } catch (PyException e) {
      return null;
    }
  }

  /**
   * Extracts the name of the operation from the script. Returns null if no name is present.
   */
  private @Nullable String extractName() {
    final Pattern namePattern = Pattern.compile("name *= *\"(.*)\" *");
    String name = null;
    String code = codeArea.getText();
    String[] lines = code.split("\n");
    for (String line : lines) {
      Matcher m = namePattern.matcher(line);
      if (m.matches()) {
        name = m.group(1).trim();
        break;
      }
    }
    return name;
  }

  /**
   * Checks if the operation name is present and not used by any other operation in the palette
   * or pipeline. This will show an alert dialog notifying the user if either does not hold.
   */
  private boolean checkName() {
    String name = extractName();
    if (name == null || name.isEmpty()) {
      Alert noName = new Alert(Alert.AlertType.ERROR);
      noName.setTitle("No Name");
      noName.setHeaderText("This operation needs a name!");
      noName.showAndWait();
    }
    if (operationNameTaken.test(name)) {
      Alert nameInUseAlert = new Alert(Alert.AlertType.ERROR);
      nameInUseAlert.setTitle("Already in Use");
      nameInUseAlert.setHeaderText("An operation already exists with the name '" + name + "'");
      nameInUseAlert.setContentText("You won't be able to save this script until you change the "
          + "name to something that doesn't belong to another operation.");
      nameInUseAlert.showAndWait();
      return true;
    }
    return false;
  }

  /**
   * Creates the name of the file to save the script to based on the operation name.
   * Returns null if the script has an error, or if the assigned name is already in use.
   */
  @SuppressWarnings("PMD")
  private @Nullable String scriptFileName() {
    String name = extractName();
    if (name != null) {
      name = name.trim().replaceAll("[ \\t]+", "_").toLowerCase(Locale.ENGLISH);
      final String regex = name + "_?([0-9]*?)\\.py"; // e.g. $name.py, $name_2.py, etc.
      boolean needsNumber = Stream.of(PythonOperationUtils.DIRECTORY.list())
          .filter(n -> n.matches(regex))
          .count() > 0;
      if (needsNumber) {
        int currentMax = Stream.of(PythonOperationUtils.DIRECTORY.list())
            .filter(n -> n.matches(regex))
            .map(n -> n.replaceAll(regex, "$1"))
            .mapToInt(n -> n.isEmpty() ? 0 : Integer.valueOf(n))
            .max()
            .orElse(1);
        name = name + '_' + (currentMax + 1);
      }
      return name.concat(".py");
    } else {
      return null;
    }
  }

  private void showScriptErrorAlert(String message) {
    Alert malformed = new Alert(Alert.AlertType.ERROR);
    malformed.setTitle("Error in script");
    malformed.setHeaderText("There is an error in the python script");
    malformed.getDialogPane().setContent(new Label(message));
    malformed.showAndWait();
  }

  /**
   * Tries to save the script to disk. Does not save if the script has an error.
   *
   * @return true if the script was saved, false otherwise
   */
  @FXML
  private boolean save() {
    if (checkName()) {
      return false;
    }
    if (scriptFile == null) {
      String fileName = scriptFileName();
      try {
        PythonScriptFile.create(codeArea.getText());
      } catch (PyException e) {
        showScriptErrorAlert(e.toString());
        return false;
      }
      scriptFile = new File(PythonOperationUtils.DIRECTORY, fileName);
    }
    try {
      Files.write(
          scriptFile.getAbsoluteFile().toPath(),
          codeArea.getText().getBytes(Charset.defaultCharset())
      );
      return true;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not save to " + scriptFile, e);
      Alert couldNotSave = new Alert(Alert.AlertType.ERROR);
      couldNotSave.setTitle("Could not save custom operation");
      couldNotSave.setContentText("Could not save to file: " + scriptFile);
      couldNotSave.showAndWait();
      return false;
    }
  }

  /**
   * Tries to 'save-as' the script to disk. Does nothing if the script has an error.
   */
  @FXML
  private void saveAs() {
    if (checkName()) {
      // No name
      return;
    }
    try {
      PythonScriptFile.create(codeArea.getText());
    } catch (PyException e) {
      // Error in script
      showScriptErrorAlert(e.toString());
      return;
    }
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(PythonOperationUtils.DIRECTORY);
    chooser.setTitle("Choose save file");
    chooser.setSelectedExtensionFilter(
        new FileChooser.ExtensionFilter("Custom GRIP operations", "*.py"));
    String fileName = scriptFileName();
    if (fileName != null) {
      chooser.setInitialFileName(fileName);
    }
    File file = chooser.showSaveDialog(getWindow());
    if (file == null) {
      // Chooser was closed; no file selected
      return;
    }
    scriptFile = file;
    save();
  }

  @FXML
  private void saveAndExit() {
    if (save() && getScript() != null) {
      // Don't exit if there's a problem with the script
      exit();
    }
  }

  @FXML
  private void exit() {
    getWindow().fireEvent(new WindowEvent(getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
  }

  @FXML
  private void openFile() {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(PythonOperationUtils.DIRECTORY);
    chooser.setTitle("Choose an operation to edit");
    chooser.setSelectedExtensionFilter(
        new FileChooser.ExtensionFilter("Custom GRIP operations", "*.py"));
    File file = chooser.showOpenDialog(getWindow());
    if (file == null) {
      // Chooser was closed; no file selected
      return;
    }
    try {
      byte[] bytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
      String code = new String(bytes, Charset.defaultCharset());
      codeArea.replaceText(code);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not read file " + file, e);
      Alert couldNotRead = new Alert(Alert.AlertType.ERROR);
      couldNotRead.setTitle("Could not read custom operation");
      couldNotRead.setContentText("Could not read from file: " + file);
      couldNotRead.showAndWait();
    }
  }

  @FXML
  private void openWiki() {
    hostServices.showDocument("https://github.com/WPIRoboticsProjects/GRIP/wiki");
  }

}
