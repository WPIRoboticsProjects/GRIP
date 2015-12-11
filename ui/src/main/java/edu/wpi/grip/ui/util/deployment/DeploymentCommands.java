package edu.wpi.grip.ui.util.deployment;


import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * The commands to be used to launch and kill the deployed program.
 */
public class DeploymentCommands {
    protected static final String DEFAULT_JAVA_COMMAND = "java";
    protected static final Function<String, String>
            DEFAULT_KILL_BY_NAME = name ->
            "kill $(ps aux | grep \"" + name + "\" | grep -v 'grep' | awk '{print $1}') || :";
    private final String javaCommand;
    private final Function<String, String> killByNameCommand;

    public static class Factory {

        public DeploymentCommands createFRC() {
            return new DeploymentCommands("/usr/local/frc/JRE/bin/java", DEFAULT_KILL_BY_NAME);
        }
    }

    DeploymentCommands(String javaCommand, Function<String, String> killByNameCommand) {
        this.javaCommand = checkNotNull(javaCommand, "The java command can not be null");
        this.killByNameCommand = checkNotNull(killByNameCommand, "The kill by name consumer can not be null");
    }

    public DeploymentCommands() {
        this(DEFAULT_JAVA_COMMAND, DEFAULT_KILL_BY_NAME);
    }

    /**
     * @param jarFile     The name of the jar file
     * @param projectFile The name of the project file
     * @return The launch command
     */
    protected String getJARLaunchCommand(String jarFile, String projectFile) {
        return this.javaCommand + " -jar " + jarFile + " " + projectFile;
    }

    /**
     * @param name The name of the process to kill
     * @return The command to kill the program running remotely
     */
    protected String getKillCommand(String name) {
        return killByNameCommand.apply(name);
    }
}
