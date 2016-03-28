
package edu.wpi.grip.core;

import edu.wpi.grip.core.serialization.Project;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;

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
    public void openProject(String projectFilePath) throws IOException {
        if (System.getProperty("grip.headless", "true").equals("true")) {
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

}
