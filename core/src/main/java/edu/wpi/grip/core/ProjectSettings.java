package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;

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

    public void setNetworkProtocol(NetworkProtocol networkProtocol) {
        this.networkProtocol = networkProtocol;
    }

    public NetworkProtocol getNetworkProtocol() {
        return networkProtocol;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public int getTeamNumber() {
        return teamNumber;
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
            return null;
        }
    }
}
