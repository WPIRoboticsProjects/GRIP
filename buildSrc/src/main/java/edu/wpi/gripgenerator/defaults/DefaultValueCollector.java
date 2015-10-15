package edu.wpi.gripgenerator.defaults;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultValueCollector {
    private Map<String, DefaultValue> defaultValueMap = new HashMap();
    private Map<String, EnumDefaultValue> defaultValueEnumNameMap = new HashMap<>();

    /**
     * @param enumDefault Adds the enumDefault to the collector
     */
    public void add(EnumDefaultValue enumDefault) {
        for (String value : enumDefault.getDefaultValues()) {
            defaultValueMap.put(value, enumDefault);
        }
        defaultValueEnumNameMap.put(enumDefault.getName(), enumDefault);
    }

    public void addAll(Collection<EnumDefaultValue> enumDefaults) {
        enumDefaults.forEach(this::add);
    }

    public void add(DefaultValue defaultValue) {
        for (String value : defaultValue.getDefaultValues()) {
            defaultValueMap.put(value, defaultValue);
        }
    }

    /**
     * Adds the given additional values to the enumeration.
     *
     * @param enumDefaultValue The enum default value to modify
     * @param additionalValues The additional values to add to the enum
     */
    public void addToEnum(EnumDefaultValue enumDefaultValue, List<String> additionalValues) {
        assert defaultValueEnumNameMap.containsValue(enumDefaultValue) : "Enum did not exist in defaultValueEnumNameMap " + enumDefaultValue;
        enumDefaultValue.getDefaultValues().addAll(additionalValues);
        for (String value : additionalValues) {
            defaultValueMap.put(value, enumDefaultValue);
        }
    }

    /**
     * Get the given enumeration given its name
     *
     * @param name The name of the enum to retrieve.
     * @return The enum that matches this name
     */
    public EnumDefaultValue getEnum(String name) {
        return defaultValueEnumNameMap.get(name);
    }

    /**
     * Checks if there is a default value for a given string key
     *
     * @param value The key to check for
     * @return True if the default value exists
     */
    public boolean hasDefaultValueFor(String value) {
        return defaultValueMap.containsKey(value);
    }

    /**
     * Retrieves the stored default value for the given key string
     *
     * @param value The value key
     * @return The DefaultValue that is relevant to this default value
     */
    public DefaultValue getDefaultValueFor(String value) {
        return defaultValueMap.get(value);
    }

}
