package edu.wpi.gripgenerator.defaults;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultValueCollector {
    private Map<String, DefaultValue> defaultValueMap = new HashMap();
    private Map<String, EnumDefaultValue> defaultValueEnumNameMap = new HashMap<>();


    public void add(EnumDefaultValue enumDefault){
        for(String value : enumDefault.getDefaultValues()){
            defaultValueMap.put(value, enumDefault);
        }
        defaultValueEnumNameMap.put(enumDefault.getName(), enumDefault);
    }

    public void addToEnum(EnumDefaultValue enumDefaultValue, List<String> additionalValues){
        assert defaultValueEnumNameMap.containsValue(enumDefaultValue) : "Enum did not exist in defaultValueEnumNameMap " + enumDefaultValue;
        enumDefaultValue.getDefaultValues().addAll(additionalValues);
        for(String value : additionalValues){
            defaultValueMap.put(value, enumDefaultValue);
        }
    }

    public EnumDefaultValue getEnum(String name){
        return defaultValueEnumNameMap.get(name);
    }

    public boolean hasDefaultValueFor(String value){
        return defaultValueMap.containsKey(value);
    }

    public DefaultValue getDefaultValueFor(String value){
        return defaultValueMap.get(value);
    }

}
