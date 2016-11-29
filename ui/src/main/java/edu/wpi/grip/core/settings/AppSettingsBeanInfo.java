package edu.wpi.grip.core.settings;

/**
 * BeanInfo class for {@link AppSettings}.  This inspects annotations on the properties in
 * AppSettings to produce PropertyDescriptors with proper display names and descriptions.
 * ControlsFX's PropertySheet control uses JavaBean properties to generate the settings editor, so
 * we need this class in order to make the properties have user-presentable names and descriptions.
 * Another way to do this without annotations would be to hardcode a bunch of PropertyDescriptors
 * here, but that would be error-prone (we would get no warning if we add a new setting and forget
 * to add a descriptor here).
 */
public class AppSettingsBeanInfo extends SimpleSettingsBeanInfo {

  public AppSettingsBeanInfo() {
    super(AppSettings.class);
  }

}
