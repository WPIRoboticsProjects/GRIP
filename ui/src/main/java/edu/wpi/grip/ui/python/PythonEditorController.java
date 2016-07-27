package edu.wpi.grip.ui.python;

import edu.wpi.grip.core.operations.python.PythonOperationUtils;
import edu.wpi.grip.core.operations.python.PythonScriptFile;
import edu.wpi.grip.ui.annotations.ParametrizedController;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   * Gets the script text in the editor.
   */
  public @Nullable String getScript() {
    if (scriptFileName() == null) {
      return null;
    } else {
      return codeArea.getText();
    }
  }

  /**
   * Creates the name of the file to save the script to based on the operation name.
   */
  private @Nullable String scriptFileName() {
    final Pattern namePattern = Pattern.compile("name *= *\"(.*)\" *");
    String code = codeArea.getText();
    String[] lines = code.split("\n");
    for (String line : lines) {
      Matcher m = namePattern.matcher(line);
      if (m.matches()) {
        return m.group(1).replaceAll("[ \\t]+", "_").toLowerCase(Locale.ENGLISH) + ".py";
      }
    }
    return null;
  }

  @FXML
  private void save() {
    if (scriptFile == null) {
      String fileName = scriptFileName();
      if (fileName == null) {
        // No 'name' property
        Alert noNameAlert = new Alert(Alert.AlertType.ERROR);
        noNameAlert.setTitle("No operation name");
        noNameAlert.setContentText("This operation needs a name!");
        noNameAlert.showAndWait();
        return;
      }
      scriptFile = new File(PythonOperationUtils.DIRECTORY, fileName);
    }
    try {
      Files.write(
          scriptFile.getAbsoluteFile().toPath(),
          codeArea.getText().getBytes(Charset.defaultCharset())
      );
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not save to " + scriptFile, e);
    }
  }

  @FXML
  private void saveAs() {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(PythonOperationUtils.DIRECTORY);
    chooser.setTitle("Choose ");
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
    save();
    exit();
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
    }
  }

  @FXML
  private void openWiki() {
    // TODO
  }

}
