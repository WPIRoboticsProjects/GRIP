package edu.wpi.grip.core.util;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class for storing runtime properties of the GRIP application.
 */
public final class GripProperties {

    private static final Properties properties = new Properties();

    /**
     * Sets the given property to the given value
     *
     * @param key   the name of the property to set
     * @param value the value of the property
     */
    public static void setProperty(String key, String value) {
        checkNotNull(key);
        checkNotNull(value);
        properties.setProperty(key, value);
    }

    /**
     * Gets the value of the given property.
     *
     * @param key the name of the property to get the value of
     * @return the value of the given property, or {@code null} if no such value exists
     */
    public static String getProperty(String key) {
        checkNotNull(key);
        return properties.getProperty(key);
    }

    /**
     * Gets the value of the given property, or the supplied default value if no value exists for that property.
     *
     * @param key          the name of the property to get the value of
     * @param defaultValue the default value to return if no value is associated with the given property
     * @return the value of the given property, or {@code defaultValue} if no value exists for that property
     */
    public static String getProperty(String key, String defaultValue) {
        checkNotNull(key);
        checkNotNull(defaultValue);
        return properties.getProperty(key, defaultValue);
    }
}
