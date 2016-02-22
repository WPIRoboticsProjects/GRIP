package edu.wpi.grip.core.operations.network.networktables;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.NetworkKeyValuePublisher;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.util.GRIPMode;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class encapsulates the way we map various settings to the global NetworkTables state.
 */
@Singleton
public class NTManager implements Manager {

    /**
     * Information from:
     * https://github.com/PeterJohnson/ntcore/blob/master/src/Log.h
     * and
     * https://github.com/PeterJohnson/ntcore/blob/e6054f543a6ab10aa27af6cace855da66d67ee44/include/ntcore_c.h#L39
     */
    private final static Map<Integer, Level> ntLogLevels = new HashMap<Integer, Level>() {{
        put(40, Level.SEVERE);
        put(30, Level.WARNING);
        put(20, Level.INFO);
        put(10, Level.FINE);
        put(9, Level.FINE);
        put(8, Level.FINE);
        put(7, Level.FINER);
        put(6, Level.FINEST);
    }};
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Inject private Pipeline pipeline;
    @Inject private PipelineRunner pipelineRunner;
    @Inject private GRIPMode gripMode;

    @Inject
    NTManager() {
        // We may have another instance of this method lying around
        NetworkTable.shutdown();

        // Redirect NetworkTables log messages to our own log files.  This gets rid of console spam, and it also lets
        // us grep through NetworkTables messages just like any other messages.
        NetworkTablesJNI.setLogger((level, file, line, msg) -> {
            String filename = new File(file).getName();
            logger.log(ntLogLevels.get(level), String.format("NetworkTables: %s:%d %s", filename, line, msg));
        }, 0);

        NetworkTable.setClientMode();

        // When in headless mode, start and stop the pipeline based on the "GRIP/run" key.  This allows robot programs
        // to control GRIP without actually restarting the process.
        NetworkTable.getTable("GRIP").addTableListener("run", (source, key, value, isNew) -> {
            if (gripMode == GRIPMode.HEADLESS) {
                if (!(value instanceof Boolean)) {
                    logger.warning("NetworkTables value GRIP/run should be a boolean!");
                    return;
                }

                if ((Boolean) value) {
                    if (!pipelineRunner.isRunning()) {
                        logger.info("Starting GRIP from NetworkTables");
                        pipelineRunner.startAsync();
                    }
                } else if (pipelineRunner.isRunning()) {
                    logger.info("Stopping GRIP from NetworkTables");
                    pipelineRunner.stopAsync();

                }
            }
        }, true);

        NetworkTable.shutdown();
    }

    /**
     * Change the server address according to the project setting.
     */
    @Subscribe
    public void updateSettings(ProjectSettingsChangedEvent event) {
        final ProjectSettings projectSettings = event.getProjectSettings();

        synchronized (NetworkTable.class) {
            NetworkTable.shutdown();
            NetworkTable.setIPAddress(projectSettings.getPublishAddress());
        }
    }

    /**
     * If there are no NTKeyValuePublishOperation steps, we can shut down NetworkTables
     */
    @Subscribe
    public void disableNetworkTables(StepRemovedEvent event) {
        if (!pipeline.getSteps().stream().anyMatch(step -> step.getOperation() instanceof NTKeyValuePublishOperation)) {
            synchronized (NetworkTable.class) {
                NetworkTable.shutdown();
            }
        }
    }


    private static final class NTPublisher<P> extends NetworkKeyValuePublisher<P> {
        private final ImmutableSet<String> keys;
        private Optional<String> name = Optional.empty();

        protected NTPublisher(Class<P> publishType, Set<String> keys) {
            super(publishType, keys);
            this.keys = ImmutableSet.copyOf(keys);
        }

        @Override
        protected void publishNameChanged(Optional<String> oldName, String newName) {
            if (oldName.isPresent()) {
                deleteOldTable(oldName.get());
            }
            this.name = Optional.of(newName);
        }

        @Override
        protected void doPublish(Map<String, P> publishValueMap) {
            publishValueMap.forEach(getTable()::putValue);
            Sets.difference(keys, publishValueMap.entrySet()).forEach(getTable()::delete);
        }

        @Override
        protected void doPublish(P value) {
            checkNotNull(value, "value cannot be null");
            getRootTable().putValue(name.get(), value);
        }

        @Override
        public void doPublish() {
            deleteOldTable(name.get());
        }


        private void deleteOldTable(String tableName) {
            final ITable root, subTable;
            synchronized (NetworkTable.class) {
                root = getRootTable();
                subTable = root.getSubTable(tableName);
            }
            keys.forEach(subTable::delete);
            root.delete(tableName);
        }

        @Override
        public void close() {
            if(name.isPresent()) {
                deleteOldTable(name.get());
            }
        }

        private ITable getTable() {
            synchronized (NetworkTable.class) {
                return getRootTable().getSubTable(name.get());
            }
        }

        private static ITable getRootTable() {
            NetworkTable.flush();
            synchronized (NetworkTable.class) {
                return NetworkTable.getTable("GRIP");
            }
        }
    }

    @Override
    public <P>NetworkKeyValuePublisher<P> createPublisher(Class<P> publishType, Set<String> keys) {
        return new NTPublisher<>(publishType, keys);
    }
}
