package edu.wpi.grip.core.settings;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.base.Throwables;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple bean info for a settings class. The settings class should subscribe to the javabean
 * standard.
 */
public class SimpleSettingsBeanInfo extends SimpleBeanInfo {

  private static final Converter<String, String> caseConverter =
      CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);

  private final Class<? extends Settings> beanClass;

  public SimpleSettingsBeanInfo(Class<? extends Settings> beanClass) {
    this.beanClass = checkNotNull(beanClass, "beanClass");
  }

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {
    Field[] fields = beanClass.getDeclaredFields();

    try {
      // For each property in the bean with a @Setting annotation, create a descriptor
      // with a display name and description included.
      List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
      for (Field field : fields) {
        String property = field.getName();
        Setting setting = field.getAnnotation(Setting.class);

        if (setting != null) {
          PropertyDescriptor descriptor = new PropertyDescriptor(
              property,
              beanClass,
              "get" + caseConverter.convert(property),
              "set" + caseConverter.convert(property)
          );
          descriptor.setDisplayName(setting.label());
          descriptor.setShortDescription(setting.description());
          propertyDescriptors.add(descriptor);
        }
      }

      return propertyDescriptors.toArray(new PropertyDescriptor[propertyDescriptors.size()]);
    } catch (IntrospectionException ex) {
      // This should only happen if an invalid argument is passed to the PropertyDescriptor
      // constructor
      throw Throwables.propagate(ex);
    }

  }
}
