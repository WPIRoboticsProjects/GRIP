package edu.wpi.grip.ui.util;

import java.io.IOException;


/**
 * A supplier that can throw an IO exception. Thus putting it on the caller to handle it instead of on
 * the creator of the lambda function.
 * @param <T> The type that the supplier returns.
 */
@FunctionalInterface
public interface SupplierWithIO<T> {
    T getWithIO() throws IOException;
}
