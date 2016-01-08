package edu.wpi.grip.core.operations.networktables;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class encapsulates the way we map various settings to the global NetworkTables state.
 */
@Singleton
public class NTManager {

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

    @Inject
    public NTManager(Logger logger) {
        // We may have another instance of this method lying around
        NetworkTable.shutdown();
        // Redirect NetworkTables log messages to our own log files.  This gets rid of console spam, and it also lets
        // us grep through NetworkTables messages just like any other messages.
        NetworkTablesJNI.setLogger((level, file, line, msg) -> {
            String fileName = "ntcore/" + file.substring(file.indexOf("src/"));
            logger.log(ntLogLevels.get(level), String.format("%s:%d %s", fileName, line, msg));
        }, 0);

        NetworkTable.setClientMode();
    }


    /**
     * Change the server address according to the project setting.
     */
    @Subscribe
    public void updateSettings(ProjectSettingsChangedEvent event) {
        final ProjectSettings projectSettings = event.getProjectSettings();

        NetworkTable.shutdown();
        if (projectSettings.getNetworkProtocol() == ProjectSettings.NetworkProtocol.NETWORK_TABLES) {
            NetworkTable.setIPAddress(projectSettings.computePublishAddress());
            NetworkTable.initialize();
        }
    }
}
