
package edu.wpi.grip.core;

import edu.wpi.grip.core.settings.ProjectSettings;

/**
 *
 */
public class MockPipeline extends Pipeline {

    private ProjectSettings settings;

    public MockPipeline(ProjectSettings settings) {
        this.settings = settings;
    }

    @Override
    public ProjectSettings getProjectSettings() {
        return settings;
    }

    public static class MockProjectSettings extends ProjectSettings {
    }

}
