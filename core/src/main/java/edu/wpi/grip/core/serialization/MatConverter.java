package edu.wpi.grip.core.serialization;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * An XStream converter for OpenCV Mats that always constructs a new, empty Mat.
 * <p>
 * <code>Mat</code> is actually a handler for a native piece of memory, and it doesn't serialize nicely.  Since the
 * only mats that need to be loaded from a project are default values, and default values for operations are really
 * only empty Mats, we can just construct a new Mat
 */
public class MatConverter implements SingleValueConverter {

    /**
     * @return The literal string "mat".  This prevents XStream from trying to serialize Mats and causing a segfault.
     */
    @Override
    public String toString(Object obj) {
        return "mat";
    }

    /**
     * @return A new empty {@link Mat}.
     */
    @Override
    public Object fromString(String str) {
        return new Mat();
    }

    /**
     * @return <code>true</code> only for {@link Mat}
     */
    @Override
    public boolean canConvert(Class type) {
        return type == Mat.class;
    }
}
