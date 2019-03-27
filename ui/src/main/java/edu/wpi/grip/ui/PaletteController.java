package edu.wpi.grip.ui;

import edu.wpi.grip.annotation.operation.OperationCategory;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.inject.Singleton;

/**
 * Controller for a list of the available operations that the user may select from.
 */
@Singleton
public class PaletteController {

  @FXML private VBox root;
  @FXML private CustomTextField operationSearch;
  @FXML private Tab allOperations;
  @FXML private Tab imgprocOperations;
  @FXML private Tab featureOperations;
  @FXML private Tab networkOperations;
  @FXML private Tab logicalOperations;
  @FXML private Tab opencvOperations;
  @FXML private Tab miscellaneousOperations;

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

    imgprocOperations.setUserData(OperationCategory.IMAGE_PROCESSING);
    featureOperations.setUserData(OperationCategory.FEATURE_DETECTION);
    networkOperations.setUserData(OperationCategory.NETWORK);
    logicalOperations.setUserData(OperationCategory.LOGICAL);
    opencvOperations.setUserData(OperationCategory.OPENCV);
    miscellaneousOperations.setUserData(OperationCategory.MISCELLANEOUS);

    // Bind the filterText of all of the individual tabs to the search field
    operationSearch.textProperty().addListener(observable -> {
      allOperations.getProperties().put(OperationListController.FILTER_TEXT, operationSearch
          .getText());
      imgprocOperations.getProperties().put(OperationListController.FILTER_TEXT, operationSearch
          .getText());
      featureOperations.getProperties().put(OperationListController.FILTER_TEXT, operationSearch
          .getText());
      networkOperations.getProperties().put(OperationListController.FILTER_TEXT, operationSearch
          .getText());
      logicalOperations.getProperties().put(OperationListController.FILTER_TEXT, operationSearch
          .getText());
      opencvOperations.getProperties().put(OperationListController.FILTER_TEXT, operationSearch
          .getText());
      miscellaneousOperations.getProperties().put(OperationListController.FILTER_TEXT,
          operationSearch.getText());
    });

    // The palette should have a lower priority for resizing than other elements
    root.getProperties().put("resizable-with-parent", false);
  }
}
