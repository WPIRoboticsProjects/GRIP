package edu.wpi.grip.core.operations.network.ros;

import com.google.common.collect.ImmutableList;
import org.ros.internal.message.Message;
import std_msgs.Bool;
import std_msgs.Float64;
import std_msgs.Float64MultiArray;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines the mapping between Java class types and the ROS types.
 * This class is designed to function like an enum but enums do not support generic types.
 */
@Immutable
public abstract class ROSType<M extends Message, J> {
    public static final ROSType<Float64, Double> FLOAT_64
            = new ROSType<Float64, Double>(Float64._TYPE, Float64.class, Double.class) {
        @Override
        void assignDataToMessage(Float64 message, Map<String, Double> data) {

        }
    };
    public static final ROSType<Float64MultiArray, double[]> FLOAT_64_ARRAY
            = new ROSType<Float64MultiArray, double[]>(Float64MultiArray._TYPE, Float64MultiArray.class, double[].class) {

        @Override
        void assignDataToMessage(Float64MultiArray message, Map<String, double[]> data) {

        }
    };
    public static final ROSType<std_msgs.String, java.lang.String> STRING
            = new ROSType<std_msgs.String, java.lang.String>(std_msgs.String._TYPE, std_msgs.String.class, java.lang.String.class) {

        @Override
        void assignDataToMessage(std_msgs.String message, Map<java.lang.String, java.lang.String> data) {

        }
    };
    private static final ROSType<std_msgs.Bool, Boolean> BOOL
            = new ROSType<Bool, Boolean>(Bool._TYPE, Bool.class, Boolean.class) {

        @Override
        void assignDataToMessage(Bool message, Map<String, Boolean> data) {

        }
    };
    private static final ImmutableList<ROSType> values = ImmutableList.of(FLOAT_64, FLOAT_64_ARRAY, STRING, BOOL);

    private final java.lang.String type;
    private final Class<M> rosType;
    private final Class<J> javaType;

    /**
     * @param type     The _TYPE from the message
     * @param rosType  The implementation of message that needs to be sent
     * @param javaType The equivalent java type.
     */
    private ROSType(java.lang.String type, Class<M> rosType, Class<J> javaType) {
        this.type = checkNotNull(type, "type cannot be null");
        this.rosType = checkNotNull(rosType, "rosType cannot be null");
        this.javaType = checkNotNull(javaType, "javaType cannot be null");
    }

    /**
     * This is necessary when trying to create a publisher of a specified type.
     * For example, when calling {@link org.ros.node.ConnectedNode#newPublisher(String, String)}
     *
     * @return The _TYPE for the given message.
     */
    public final java.lang.String getType() {
        return type;
    }

    /**
     * @param message The message of unknown type to set the data to
     * @param data    The data to assign into the body of the message.
     */
    public final void assignData(Message message, Map<String, J> data) {
        checkNotNull(message, "Message cannot be null");
        final M castMessage = rosType.cast(message);
        assignDataToMessage(castMessage, data);
    }

    abstract void assignDataToMessage(M message, Map<String, J> data);


    /**
     * Takes a given value and resolves it to a ros type.
     *
     * @param value The value to resolve the equivalent ROSType for
     * @return The ROSType that maps to the specified object's type
     * @throws IllegalArgumentException if the class type is not supported.
     */
    public static ROSType resolveType(final Object value) {
        checkNotNull(value, "value can not be null");
        for (ROSType type : values) {
            if (type.javaType.isInstance(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported Type: " + value.getClass().getCanonicalName());
    }
}
