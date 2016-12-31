package edu.wpi.grip.core.settings;

/**
 * BeanInfo class for {@link ProjectSettings}.  This inspects annotations on the properties in
 * ProjectSettings to produce PropertyDescriptors with proper display names and descriptions.
 * ControlsFX's PropertySheet control uses JavaBean properties to generate the settings editor, so
 * we need this class in order to make the properties have user-presentable names and descriptions.
 * Another way to do this without annotations would be to hardcode a bunch of PropertyDescriptors
 * here, but that would be error-prone (we would get no warning if we add a new setting and forget
 * to add a descriptor here).
 */
public class ProjectSettingsBeanInfo extends SimpleSettingsBeanInfo {

  public ProjectSettingsBeanInfo() {
    super(ProjectSettings.class);
  }

}
