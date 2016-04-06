package edu.wpi.grip.core.settings;

import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;

/**
 * Provides access to the project's settings.
 * This should be injected to get access to the settings.
 */
public interface SettingsProvider {
    /**
     * @return The current per-project settings.  This object may become out of date if the settings are edited
     * by the user, so objects requiring a preference value should also subscribe to {@link ProjectSettingsChangedEvent}
     * to get updates.
     */
    ProjectSettings getProjectSettings();
}
