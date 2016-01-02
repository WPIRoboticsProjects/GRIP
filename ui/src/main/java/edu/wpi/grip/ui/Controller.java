package edu.wpi.grip.ui;

import javafx.scene.Node;

/**
 * A controller should always provide a method to get the root node that it is controlling.
 * This allows the controllers root node to be added to other UI components.
 */
public interface Controller {
    /**
     * @return The root node of the controller
     */
    Node getRoot();
}
