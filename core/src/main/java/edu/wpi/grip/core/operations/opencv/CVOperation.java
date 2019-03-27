package edu.wpi.grip.core.operations.opencv;

import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.util.Icon;

public interface CVOperation extends Operation {

  /**
   * Default builder for OpenCV Operations.
   */
  static OperationDescription.Builder defaultBuilder() {
    return OperationDescription.builder()
        .category(OperationCategory.OPENCV)
        .icon(Icon.iconStream("opencv"));
  }

  static OperationDescription defaults(String name, String description) {
    return defaultBuilder().name(name).summary(description).build();
  }

  static OperationDescription defaults(String name, String summary, String... aliases) {
    return defaultBuilder().name(name).summary(summary).aliases(aliases).build();
  }

}
