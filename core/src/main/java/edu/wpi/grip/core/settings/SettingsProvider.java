package edu.wpi.grip.core.settings;

/**
 * Provides access to the project's settings. This should be injected to get access to the
 * settings.
 */
public interface SettingsProvider {
  /**
   * This object may become out of date if the settings are edited by the user, so objects requiring
   * a preference value should also subscribe to
   * {@link edu.wpi.grip.core.events.ProjectSettingsChangedEvent}
   * to get updates.
   *
   * @return The current per-project settings.
   */
  ProjectSettings getProjectSettings();
}
