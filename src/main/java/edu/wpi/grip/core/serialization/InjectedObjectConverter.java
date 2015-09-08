package edu.wpi.grip.core.serialization;

import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * An XStream converter that, instead of serializing and deserializing an object, injects a single injected value
 * for all instances.
 */
class InjectedObjectConverter<T> implements SingleValueConverter {
    private final T obj;

    public InjectedObjectConverter(T obj) {
        this.obj = obj;
    }

    /**
     * @return The literal string "injected" for any object.  This prevents XStream from attempting to serialize
     * objects that shouldn't be serialized.
     */
    @Override
    public String toString(Object object) {
        return "injected";
    }

    /**
     * @return The specified object to be injected in the deserialized class
     */
    @Override
    public Object fromString(String str) {
        return this.obj;
    }

    /**
     * @return <code>true</code> only for an object the same type as the object passed in the constructor
     */
    @Override
    public boolean canConvert(Class type) {
        return this.obj.getClass().equals(type);
    }
}