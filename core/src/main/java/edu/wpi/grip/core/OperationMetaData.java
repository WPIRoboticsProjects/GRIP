package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;

import javax.annotation.concurrent.Immutable;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds metadata for an operation.
 */
@Immutable
public class OperationMetaData {

    private final OperationDescription description;
    private final Supplier<Operation> operationSupplier;

    /**
     * Creates a metadata object for an {@link Operation}.
     *
     * @param description       the summary for the {@code Operation}
     * @param operationSupplier a supplier for the {@code Operation}. This should return a new instance each time it's called.
     */
    public OperationMetaData(OperationDescription description, Supplier<Operation> operationSupplier) {
        this.description = checkNotNull(description);
        this.operationSupplier = checkNotNull(operationSupplier);
    }

    /**
     * Gets the summary of the operation.
     */
    public OperationDescription getDescription() {
        return description;
    }

    /**
     * Gets a {@code Supplier} for the operation. This should return a new instance each time it's called.
     */
    public Supplier<Operation> getOperationSupplier() {
        return operationSupplier;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(OperationMetaData.class)
                .add("description", description)
                .add("operationSupplier", operationSupplier)
                .toString();
    }
}
