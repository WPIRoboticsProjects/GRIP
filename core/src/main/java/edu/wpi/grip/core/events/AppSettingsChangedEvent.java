package edu.wpi.grip.core.events;

import edu.wpi.grip.core.settings.AppSettings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when the app settings are changed.
 */
public class AppSettingsChangedEvent {

  private final AppSettings appSettings;

  public AppSettingsChangedEvent(AppSettings appSettings) {
    this.appSettings = checkNotNull(appSettings, "appSettings");
  }

  public AppSettings getAppSettings() {
    return appSettings;
  }

}
