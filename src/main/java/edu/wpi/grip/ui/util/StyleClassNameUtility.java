package edu.wpi.grip.ui.util;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Step;

/**
 * Creates CSS style classes and ID's for nodes.  This makes it possible to use CSS selectors to retrieve nodes in unit
 * tests.
 */
public final class StyleClassNameUtility {

    private StyleClassNameUtility() {
    }


    /**
     * Returns the CSS id name for an operation
     *
     * @param operation The operation to get the class name for
     * @return The CSS id for the operation. To use as a css selector then prepend the string with a '#'
     */
    public static String idNameFor(Operation operation) {
        return shortNameFor(operation).append("-operation").toString();
    }

    /**
     * Return the CSS class name for a step
     *
     * @param step The step to get the class name for
     * @return The CSS class for the step in the pipeline. To use as a css selector then prepend the string with a '.'
     */
    public static String classNameFor(Step step) {
        return shortNameFor(step.getOperation()).append("-step").toString();
    }

    /**
     * Return the CSS class name for a connection.
     *
     * @param connection The connection to get the class name for.
     * @return The CSS class for the connection in the pipeline. To use as a css selector then prepend the string with a '.'
     */
    public static String classNameFor(Connection connection) {
        return "connection-" +
                connection.getOutputSocket().getSocketHint().getIdentifier()
                + "-to-" +
                connection.getInputSocket().getSocketHint().getIdentifier();
    }

    private static StringBuilder shortNameFor(Operation operation) {
        return new StringBuilder(operation.getName().toLowerCase().replace(" ", "-"));
    }
}
