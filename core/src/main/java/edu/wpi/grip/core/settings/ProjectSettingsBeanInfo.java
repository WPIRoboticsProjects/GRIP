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

/**
 * BeanInfo class for {@link ProjectSettings}.  This inspects annotations on the properties in ProjectSettings to
 * produce PropertyDescriptors with proper display names and descriptions.
 * <p>
 * ControlsFX's PropertySheet control uses JavaBean properties to generate the settings editor, so we need this class
 * in order to make the properties have user-presentable names and descriptions.
 * <p>
 * Another way to do this without annotations would be to hardcode a bunch of PropertyDescriptors here, but that would
 * be error-prone (we would get no warning if we add a new setting and forget to add a descriptor here).
 * <p>
 * This class is never run in headless mode, so the nonexistance of the JavaBeans API and the slowness of reflection on
 * the roboRIO is not an issue.
 */
public class ProjectSettingsBeanInfo extends SimpleBeanInfo {

    private final Converter<String, String> caseConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);

    public PropertyDescriptor[] getPropertyDescriptors() {
        Field[] fields = ProjectSettings.class.getDeclaredFields();

        try {
            // For each property in ProjectSettings with a @Setting annotation, create a descriptor with a display name
            // and description included.
            List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
            for (Field field : fields) {
                String property = field.getName();
                Setting setting = field.getAnnotation(Setting.class);

                if (setting != null) {
                    PropertyDescriptor descriptor = new PropertyDescriptor(property, ProjectSettings.class,
                            "get" + caseConverter.convert(property), "set" + caseConverter.convert(property));
                    descriptor.setDisplayName(setting.label());
                    descriptor.setShortDescription(setting.description());
                    propertyDescriptors.add(descriptor);
                }
            }

            return propertyDescriptors.toArray(new PropertyDescriptor[propertyDescriptors.size()]);
        } catch (IntrospectionException e) {
            // This should only happen if an invalid argument is passed to the PropertyDescriptor constructor
            throw Throwables.propagate(e);
        }
    }
}
