package edu.wpi.grip.ui.util.deployment;

import com.google.inject.Singleton;
import com.jcabi.ssh.SSHByPassword;
import com.jcabi.ssh.Shell;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains all of the details for securely copying grip core and a save file to another device.
 */
public class SecureShellDetails {
    private static final int DEFAULT_PORT = 22;

    private final String userSSH;
    private final Optional<String> password;
    private final String host;
    private final Optional<String> remoteDir;
    private final int port;

    @Singleton
    public static class Factory {
        SecureShellDetails createFRC(InetAddress address) {
            return new SecureShellDetails("lvuser", address.getHostAddress());
        }
    }

    /**
     * @param userSSH   The username to connect to
     * @param password  The password to use, nullable
     * @param host      The host's address
     * @param remoteDir The remote directory to ssh into
     * @param port      The port to to use.
     */
    public SecureShellDetails(String userSSH, String password, String host, String remoteDir, int port) {
        this.userSSH = checkNotNull(userSSH, "userSSH can not be null");
        this.password = Optional.ofNullable(password);
        this.host = checkNotNull(host, "host can not be null");
        this.remoteDir = Optional.ofNullable(remoteDir);
        this.port = port;
    }

    public SecureShellDetails(String userSSH, String password, String serverSSH, String remoteDir) {
        this(userSSH, password, serverSSH, remoteDir, DEFAULT_PORT);
    }

    public SecureShellDetails(String userSSH, String host, String remoteDir) {
        this(userSSH, null, host, remoteDir, DEFAULT_PORT);
    }

    public SecureShellDetails(String userSSH, String host) {
        this(userSSH, null, host, null, DEFAULT_PORT);
    }


    protected int getPort() {
        return port;
    }

    protected String host() {
        return host;
    }

    public String getUserSSH() {
        return userSSH;
    }

    protected Optional<String> getPassword() {
        return password;
    }

    protected Scp createSCPRunner() {
        Scp scp = new Scp();
        scp.setPort(getPort());
        scp.setPassword(getPassword().orElse(""));
        scp.setTodir(getToDir());
        scp.setProject(new Project());
        scp.setTrust(true);
        return scp;
    }

    protected Shell createSSHShell() throws UnknownHostException {
        return new SSHByPassword(host(), getPort(), getUserSSH(), getPassword().orElse(""));
    }

    /**
     * @return The directory to SCP the files to
     */
    protected String getToDir() {
        // userSSH + ":" + password + "@" + srvrSSH + ":" + remoteDir;
        String sshDirCommand = userSSH;
        if (password.isPresent()) {
            sshDirCommand += (":" + password.get());
        }
        sshDirCommand += ("@" + host + ":");
        if (remoteDir.isPresent()) {
            sshDirCommand += remoteDir.get();
        }
        return sshDirCommand;
    }
}
