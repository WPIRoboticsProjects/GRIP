
package edu.wpi.grip.core;

import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.util.GripProperties;

import javafx.application.Platform;

import java.io.File;
import java.io.IOException;

/**
 * Class used for opening GRIP projects.
 */
public class ProjectManager {

    private final Project project;

    public ProjectManager(final Project project) {
        this.project = project;
    }

    /**
     * Opens a a GRIP project at the given location
     *
     * @param projectFilePath the path to the project to open
     * @throws IOException if the file couldn't be read
     */
    public void openProjectFile(String projectFilePath) throws IOException {
        if (GripProperties.getProperty("headless").equals("true")) {
            project.open(new File(projectFilePath));
        } else {
            // Need to be careful - this will cause a deadlock if not called from the JavaFX application thread
            Platform.runLater(() -> {
                try {
                    project.open(new File(projectFilePath));
                } catch (IOException ignore) {

                }
            });
        }
    }

    /**
     * Opens the GRIP project serialized to the given string.
     *
     * @param projectXml the serialized GRIP project to open
     */
    public void openProject(String projectXml) {
        if (GripProperties.getProperty("headless").equals("true")) {
            project.open(projectXml);
        } else {
            // Need to be careful - this will cause a deadlock if not called from the JavaFX application thread
            Platform.runLater(() -> project.open(projectXml));
        }
    }

}
