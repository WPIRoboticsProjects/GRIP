package edu.wpi.grip.ui;

import edu.wpi.grip.core.OperationDescription;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import javax.inject.Singleton;

/**
 * Controller for a list of the available operations that the user may select from.
 */
@Singleton
public class PaletteController {

  @FXML private VBox root;
  @FXML private CustomTextField operationSearch;
  @FXML private TitledPane allOperations;
  @FXML private TitledPane imgprocOperations;
  @FXML private TitledPane featureOperations;
  @FXML private TitledPane networkOperations;
  @FXML private TitledPane logicalOperations;
  @FXML private TitledPane opencvOperations;
  @FXML private TitledPane miscellaneousOperations;
  @FXML private TitledPane customOperations;

  @FXML
  protected void initialize() {
    // Make the search box have a "clear" button. This is the only way to do this unfortunately.
    // https://bitbucket.org/controlsfx/controlsfx/issues/330/making-textfieldssetupclearbuttonfield
    try {
      final Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField
          .class, ObjectProperty.class);
      m.setAccessible(true);
      m.invoke(null, operationSearch, operationSearch.rightProperty());
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    imgprocOperations.setUserData(OperationDescription.Category.IMAGE_PROCESSING);
    featureOperations.setUserData(OperationDescription.Category.FEATURE_DETECTION);
    networkOperations.setUserData(OperationDescription.Category.NETWORK);
    logicalOperations.setUserData(OperationDescription.Category.LOGICAL);
    opencvOperations.setUserData(OperationDescription.Category.OPENCV);
    miscellaneousOperations.setUserData(OperationDescription.Category.MISCELLANEOUS);
    customOperations.setUserData(OperationDescription.Category.CUSTOM);

    // Bind the filterText of all of the individual tabs to the search field
    operationSearch.textProperty().addListener(observable -> {
      allOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      imgprocOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      featureOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      networkOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      logicalOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      opencvOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      miscellaneousOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
      customOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
    });

    // The palette should have a lower priority for resizing than other elements
    root.getProperties().put("resizable-with-parent", false);
  }
}
