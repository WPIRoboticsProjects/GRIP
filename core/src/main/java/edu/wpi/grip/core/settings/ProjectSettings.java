package edu.wpi.grip.core.settings;

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

    @Setting(label = "Network Protocol", description = "The protocol to use to publish results")
    private NetworkProtocol networkProtocol = NetworkProtocol.NONE;

    @Setting(label = "FRC Team Number", description = "The team number, if used for FRC")
    private int teamNumber = 0;

    @Setting(label = "NetworkTables Server Address", description = "The host that runs the Network Protocol server. " +
            "If not specified and NetworkTables is specified as the protocol, the hostname is derived from the team " +
            "number.")
    private String publishAddress = "";

    @Setting(label = "Deploy Address", description = "The remote host that grip should be remotely deployed to. If " +
            "not specified, the hostname is derived from the team number.")
    private String deployAddress = "";

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

    public void setPublishAddress(String publishAddress) {
        this.publishAddress =
                checkNotNull(publishAddress, "Network Protocol Server Address cannot be null");
    }

    public String getPublishAddress() {
        return publishAddress;
    }

    public void setDeployAddress(String deployAddress) {
        this.deployAddress = checkNotNull(deployAddress, "Deploy Address can not be null");
    }

    public String getDeployAddress() {
        return deployAddress;
    }

    /**
     * @return The address of the machine that the NetworkTables server is running on.  If
     * {@link #setPublishAddress} is specified, that is returned, otherwise this is based on the team
     * number.
     */
    public String computePublishAddress() {
        if (publishAddress == null || publishAddress.isEmpty()) {
            return "roborio-" + teamNumber + "-frc.local";
        } else {
            return publishAddress;
        }
    }

    public String computeDeployAddress() {
        if (deployAddress == null || deployAddress.isEmpty()) {
            return "roborio-" + teamNumber + "-frc.local";
        } else {
            return publishAddress;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("networkProtocol", networkProtocol)
                .add("publishAddress", publishAddress)
                .add("deployAddress", deployAddress)
                .add("teamNumber", teamNumber)
                .toString();
    }

    @Override
    @SuppressWarnings("PMD.CloneThrowsCloneNotSupportedException")
    public ProjectSettings clone() {
        try {
            return (ProjectSettings) super.clone();
        } catch (CloneNotSupportedException impossible) {
            Throwables.propagate(impossible);
        }

        return null;
    }
}
