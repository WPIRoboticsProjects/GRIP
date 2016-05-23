package edu.wpi.grip.core.operations.opencv;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.util.Icon;

public interface CVOperation extends Operation {

    static OperationDescription.Builder defaultBuilder() {
        return OperationDescription.builder()
                .category(OperationDescription.Category.OPENCV)
                .icon(Icon.iconStream("opencv"));
    }

    static OperationDescription defaults(String name, String description) {
        return defaultBuilder().name(name).summary(description).build();
    }
}
