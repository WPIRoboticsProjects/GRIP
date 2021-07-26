package edu.wpi.grip.core.operations.network.networktables;

import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.MapNetworkPublisher;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.MapNetworkReceiverFactory;
import edu.wpi.grip.core.operations.network.NetworkReceiver;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.util.GripMode;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.LogMessage;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.NetworkTablesJNI;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class encapsulates the way we map various settings to the global NetworkTables state.
 */
@Singleton
public class NTManager implements Manager, MapNetworkPublisherFactory, MapNetworkReceiverFactory {

  /**
   * Information from: https://github.com/PeterJohnson/ntcore/blob/master/src/Log.h and
   * https://github.com/PeterJohnson/ntcore/blob/e6054f543a6ab10aa27af6cace855da66d67ee44
   * /include/ntcore_c.h#L39
   */
  protected static final Map<Integer, Level> ntLogLevels = ImmutableMap.<Integer, Level>builder()
      .put(50, Level.SEVERE) // "Critical"
      .put(40, Level.SEVERE)
      .put(30, Level.WARNING)
      .put(20, Level.INFO)
      .put(10, Level.FINE)
      .put(9, Level.FINE)
      .put(8, Level.FINE)
      .put(7, Level.FINER)
      .put(6, Level.FINEST)
      .build();

  private static final Logger logger = Logger.getLogger(NTManager.class.getName());
  private static final Object ntLock = new Object();
  private final NetworkTableInstance ntInstance;
  @Inject
  private PipelineRunner pipelineRunner;
  @Inject
  private GripMode gripMode;

  @Inject
  @VisibleForTesting
  @SuppressWarnings("JavadocMethod")
  public NTManager(NetworkTableInstance ntInstance) {
    this.ntInstance = Objects.requireNonNull(ntInstance, "ntInstance");
    // Redirect NetworkTables log messages to our own log files.  This gets rid of console spam,
    // and it also lets us grep through NetworkTables messages just like any other messages.
    ntInstance.addLogger(NTManager::logNtMessage, 0, 50);

    ntInstance.startClient();

    // When in headless mode, start and stop the pipeline based on the "GRIP/run" key.  This
    // allows robot programs to control GRIP without actually restarting the process.
    ntInstance.getTable("GRIP").addEntryListener("run", (source, key, entry, value, flags) -> {
      if (gripMode == GripMode.HEADLESS) {
        if (value.getType() != NetworkTableType.kBoolean) {
          logger.warning("NetworkTables value GRIP/run should be a boolean!");
          return;
        }

        if (value.getBoolean()) {
          if (!pipelineRunner.isRunning()) {
            logger.info("Starting GRIP from NetworkTables");
            pipelineRunner.startAsync();
          }
        } else if (pipelineRunner.isRunning()) {
          logger.info("Stopping GRIP from NetworkTables");
          pipelineRunner.stopAsync();

        }
      }
    }, EntryListenerFlags.kImmediate
        | EntryListenerFlags.kNew
        | EntryListenerFlags.kDelete
        | EntryListenerFlags.kUpdate);
  }

  private static void logNtMessage(LogMessage logMessage) {
    String file = logMessage.filename;
    int level = logMessage.level;
    String filename = new File(file).getName();
    logger.log(
        ntLogLevels.get(level),
        String.format(
            "NetworkTables: %s:%d %s",
            filename,
            logMessage.line,
            logMessage.message)
    );
  }

  /**
   * Change the server address according to the project setting.
   */
  @Subscribe
  public void updateSettings(ProjectSettingsChangedEvent event) {
    final ProjectSettings projectSettings = event.getProjectSettings();

    synchronized (ntLock) {
      ntInstance.stopClient();
      ntInstance.deleteAllEntries();
      ntInstance.startClient();
      ntInstance.setServer(projectSettings.getPublishAddress());
    }
  }

  /**
   * Flush all changes to networktables when the pipeline completes.
   */
  @Subscribe
  public void flushOnPipelineComplete(@Nullable RunStoppedEvent event) {
    synchronized (ntLock) {
      ntInstance.flush();
    }
  }

  @Override
  public <P> MapNetworkPublisher<P> create(Set<String> keys) {
    return new NTPublisher<>(ntInstance, keys);
  }

  @Override
  public NetworkReceiver create(String path) {
    return new NTReceiver(ntInstance, path);
  }

  private static final class NTReceiver extends NetworkReceiver {

    private final NetworkTableInstance ntInstance;
    private final List<Consumer<Object>> listeners = new LinkedList<>();
    private int entryListenerFunctionUid;
    private Object object = false;

    protected NTReceiver(NetworkTableInstance ntInstance, String path) {
      super(path);
      this.ntInstance = ntInstance;
      addListener();
    }

    private void addListener() {
      entryListenerFunctionUid = ntInstance.addEntryListener(
          path,
          entryNotification -> {
            object = entryNotification.value.getValue();
            listeners.forEach(c -> c.accept(object));
          },
          EntryListenerFlags.kImmediate
              | EntryListenerFlags.kLocal
              | EntryListenerFlags.kNew
              | EntryListenerFlags.kUpdate
              | EntryListenerFlags.kDelete
      );
    }

    @Override
    public void addListener(Consumer<Object> consumer) {
      listeners.add(consumer);
    }

    @Override
    public Object getValue() {
      return object;
    }

    @Override
    public void close() {
      NetworkTablesJNI.removeEntryListener(entryListenerFunctionUid);
    }
  }

  private static final class NTPublisher<P> extends MapNetworkPublisher<P> {
    private final NetworkTableInstance ntInstance;
    private final ImmutableSet<String> keys;
    private Optional<String> name = Optional.empty();

    protected NTPublisher(NetworkTableInstance ntInstance, Set<String> keys) {
      super(keys);
      this.ntInstance = ntInstance;
      this.keys = ImmutableSet.copyOf(keys);
    }

    private NetworkTable getRootTable() {
      synchronized (ntLock) {
        return ntInstance.getTable("GRIP");
      }
    }

    @Override
    protected void publishNameChanged(Optional<String> oldName, String newName) {
      oldName.ifPresent(this::deleteOldTable);
      this.name = Optional.of(newName);
    }

    @Override
    public void doPublish() {
      deleteOldTable(name.get());
    }

    @Override
    protected void doPublish(Map<String, P> publishValueMap) {
      publishValueMap.forEach((key, value) -> getTable().getEntry(key).setValue(value));
      Sets.difference(keys, publishValueMap.keySet()).forEach(getTable()::delete);
    }

    @Override
    protected void doPublishSingle(P value) {
      checkNotNull(value, "value cannot be null");
      getRootTable().getEntry(name.get()).setValue(value);
    }

    private void deleteOldTable(String tableName) {
      synchronized (ntLock) {
        final NetworkTable root = getRootTable();
        final NetworkTable subTable = root.getSubTable(tableName);
        keys.forEach(subTable::delete);
        root.delete(tableName);
      }
    }

    @Override
    public void close() {
      name.ifPresent(this::deleteOldTable);
    }

    private NetworkTable getTable() {
      synchronized (ntLock) {
        return getRootTable().getSubTable(name.get());
      }
    }
  }
}
