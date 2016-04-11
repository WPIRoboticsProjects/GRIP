package edu.wpi.grip.ui.util;


import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Convenient utility for viewing the scene graph in a hierarchical layout.
 */
public final class DebugUtility {
    private DebugUtility() {
        /* no-op */
    }

    /**
     * @param base The root element to start the tree at.
     * @return A tabbed tree of the scene using each {@link Node nodes} {@link Object#toString()}
     */
    public static String sceneGraphToTree(Parent base) {
        final StringBuilder builder = new StringBuilder();
        buildSceneGraph(builder, "", base);
        return builder.toString();
    }

    private static void buildSceneGraph(StringBuilder builder, String tabs, Node node) {
        // Append this element to the tree
        builder.append(tabs).append(node.toString()).append('\n');
        // If we have a parent node then crawl its scene graph.
        if (node instanceof Parent) {
            final Parent nodeAsParent = (Parent) node;
            nodeAsParent.getChildrenUnmodifiable().forEach(child -> {
                buildSceneGraph(builder, tabs + "\t", child);
            });
        }
    }
}
