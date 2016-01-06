package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This object holds settings that are saved in project files.  This includes things like team numbers, which need to
 * be preserved when deploying the project.
 */
public class ProjectSettings implements Cloneable {

    /**
     * What protocol to use to publish values.  Only NetworkTables is supported right now, but in the future we may
     * make the publish operations work with other protocols like ROS.
     */
    public enum NetworkProtocol {
        NETWORK_TABLES,
        NONE
    }

    private NetworkProtocol networkProtocol = NetworkProtocol.NETWORK_TABLES;
    private int teamNumber = 0;
    private String networkTablesServer = "";

    public void setNetworkProtocol(NetworkProtocol networkProtocol) {
        this.networkProtocol = checkNotNull(networkProtocol, "Network protocol cannot be null");
    }

    public NetworkProtocol getNetworkProtocol() {
        return networkProtocol;
    }

    public void setTeamNumber(int teamNumber) {
        checkArgument(teamNumber >= 0, "Team number cannot be negative");
        this.teamNumber = teamNumber;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setNetworkTablesServer(String networkTablesServer) {
        this.networkTablesServer =
                checkNotNull(networkTablesServer, "NetworkTables server cannot be null");
    }

    public String getNetworkTablesServer() {
        return networkTablesServer;
    }

    /**
     * @return The address of the machine that the NetworkTables server is running on.  If
     * {@link #setNetworkTablesServer} is specified, that is returned, otherwise this is based on the team number.
     */
    public String computeNetworkTablesServer() {
        if (networkTablesServer.isEmpty()) {
            return "roboio-" + teamNumber + "-frc.local";
        } else {
            return networkTablesServer;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("networkProtocol", networkProtocol)
                .add("teamNumber", teamNumber)
                .toString();
    }

    @Override
    public ProjectSettings clone() {
        try {
            return (ProjectSettings) super.clone();
        } catch (CloneNotSupportedException impossible) {
            Throwables.propagate(impossible);
        }

        return null;
    }
}
