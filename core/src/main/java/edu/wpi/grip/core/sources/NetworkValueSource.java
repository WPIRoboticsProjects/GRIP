package edu.wpi.grip.core.sources;


import com.google.common.base.StandardSystemProperty;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.service.LoggingListener;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides a way to get a value from NetworkTables.
 */
@XStreamAlias(value = "grip:NetworkValue")
public class NetworkValueSource extends Source {

    private final static String KEY_PROPERTY = "key";
    private final static String TYPE_PROPERTY = "type";
    private static final Logger logger = Logger.getLogger(NetworkValueSource.class.getName());

    private final String name;

    private final EventBus eventBus;
    private final Properties properties;

    private final SocketHint<Number> numberOutputHint = SocketHints.createNumberSocketHint("Value", 0);
    private final SocketHint<Boolean> booleanOutputHint = SocketHints.createBooleanSocketHint("Value", false);
    private final SocketHint<String> stringOutputHint = SocketHints.Outputs.createTextSocketHint("Value", "");
    private final OutputSocket<Number> numberOutputSocket;
    private final OutputSocket<Boolean> booleanOutputSocket;
    private final OutputSocket<String> stringOutputSocket;
    private final OutputSocket outputSocket;
    private final String key;
    private volatile boolean booleanPrevValue = false;
    private volatile double numberPrevValue = 0;
    private volatile String stringPrevValue = "";
    private ITable table = NetworkTable.getTable("GRIP");

    public enum ValueType {
        BOOLEAN,
        NUMBER,
        STRING,
    }
    private final ValueType valueType;

    public interface Factory {
        NetworkValueSource create(String key, ValueType valueType) throws IOException;

        NetworkValueSource create(Properties properties) throws IOException;
    }

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus The EventBus to attach to
     * @param address  A URL to stream video from an IP camera
     */
    @AssistedInject
    NetworkValueSource(
            final EventBus eventBus,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final String key,
            @Assisted final ValueType valueType) throws IOException {
        this(eventBus, exceptionWitnessFactory, createProperties(key, valueType));
    }

    /**
     * Used for serialization
     */
    @AssistedInject
    NetworkValueSource(
            final EventBus eventBus,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final Properties properties) {
        super(exceptionWitnessFactory);
        this.eventBus = eventBus;
        this.properties = properties;

        final String keyProperty = properties.getProperty(KEY_PROPERTY);
        if (keyProperty == null) {
            throw new IllegalArgumentException("Cannot initialize NetworkValueSource without a key");
        }
        this.name = "Network Value " + keyProperty;
        this.key = keyProperty;

        final String typeProperty = properties.getProperty(TYPE_PROPERTY);
        if (typeProperty == null) {
            throw new IllegalArgumentException("Cannot initialize NetworkValueSource without a type");
        }
        if (typeProperty.equals("BOOLEAN")) {
            this.booleanOutputSocket = new OutputSocket<>(eventBus, booleanOutputHint);
            this.numberOutputSocket = null;
            this.stringOutputSocket = null;
            this.outputSocket = booleanOutputSocket;
            this.valueType = ValueType.BOOLEAN;
        } else if (typeProperty.equals("NUMBER")) {
            this.booleanOutputSocket = null;
            this.numberOutputSocket = new OutputSocket<>(eventBus, numberOutputHint);
            this.stringOutputSocket = null;
            this.outputSocket = numberOutputSocket;
            this.valueType = ValueType.NUMBER;
        } else if (typeProperty.equals("STRING")) {
            this.booleanOutputSocket = null;
            this.numberOutputSocket = null;
            this.stringOutputSocket = new OutputSocket<>(eventBus, stringOutputHint);
            this.outputSocket = stringOutputSocket;
            this.valueType = ValueType.STRING;
        } else {
            throw new IllegalArgumentException("NetworkValueSource does not support type " + typeProperty);
        }

        table.addTableListener(key, (source, key, value, isNew) -> {
            eventBus.post(new SourceHasPendingUpdateEvent(this));
        }, true);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OutputSocket[] createOutputSockets() {
        return new OutputSocket[]{outputSocket};
    }

    @Override
    protected boolean updateOutputSockets() {
        if (valueType == ValueType.BOOLEAN) {
            boolean newValue = table.getBoolean(key, false);
            // We have a new value then we need to update the socket value
            if (newValue != booleanPrevValue) {
                booleanPrevValue = newValue;
                // Update the value
                booleanOutputSocket.setValue(newValue);
                // We have updated output sockets
                return true;
            } else {
                return false; // No output sockets were updated
            }
        } else if (valueType == ValueType.NUMBER) {
            double newValue = table.getNumber(key, 0);
            // We have a new value then we need to update the socket value
            if (newValue != numberPrevValue) {
                numberPrevValue = newValue;
                // Update the value
                numberOutputSocket.setValue(newValue);
                // We have updated output sockets
                return true;
            } else {
                return false; // No output sockets were updated
            }
        } else if (valueType == ValueType.STRING) {
            String newValue = table.getString(key, "");
            // We have a new value then we need to update the socket value
            if (!newValue.equals(stringPrevValue)) {
                stringPrevValue = newValue;
                // Update the value
                stringOutputSocket.setValue(newValue);
                // We have updated output sockets
                return true;
            } else {
                return false; // No output sockets were updated
            }
        } else {
            return false;
        }
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public void initialize() {
    }

    private static Properties createProperties(String key, ValueType valueType) {
        final Properties properties = new Properties();
        properties.setProperty(KEY_PROPERTY, key);
        properties.setProperty(TYPE_PROPERTY, valueType.toString());
        return properties;
    }
}
