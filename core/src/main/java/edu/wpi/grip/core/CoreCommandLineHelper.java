package edu.wpi.grip.core;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.List;

/**
 * A helper class for command line options for GRIP.
 */
public class CoreCommandLineHelper {

  public static final String FILE_OPTION = "f"; // "f" for "file"
  public static final String PORT_OPTION = "p"; // "p" for "port"
  public static final String HELP_OPTION = "h"; // "h" for "help" (this is standard)
  public static final String VERSION_OPTION = "v"; // "v" for "version" (this is standard)

  private final Options options = new Options();
  private static final Option saveOption =
      Option.builder(FILE_OPTION)
          .longOpt("file")
          .desc("Set the GRIP save file to load")
          .hasArg()
          .numberOfArgs(1)
          .argName("path")
          .build();
  private static final Option portOption =
      Option.builder(PORT_OPTION)
          .longOpt("port")
          .desc("Set the port to run the HTTP server on")
          .hasArg()
          .numberOfArgs(1)
          .argName("port")
          .build();
  private static final Option helpOption
      = new Option(HELP_OPTION, "help", false, "Prints the command line options");
  private static final Option versionOption
      = new Option(VERSION_OPTION, "version", false, "Prints the version of GRIP");

  /**
   * Creates a new core commandline helper with the standard options.
   */
  public CoreCommandLineHelper() {
    options.addOption(saveOption);
    options.addOption(portOption);
    options.addOption(helpOption);
    options.addOption(versionOption);
  }

  /**
   * Creates a command line helper with all the standard options, plus any additional options
   * required by subclasses.
   *
   * @param additionalOptions additional command line arguments
   */
  protected CoreCommandLineHelper(Option... additionalOptions) {
    this();
    for (Option o : additionalOptions) {
      options.addOption(o);
    }
  }

  /**
   * Parses an array of command line arguments. If there are unknown arguments or the arguments are
   * otherwise malformed, a help message will be printed and the application will exit without
   * returning from this method. This will also occur if the help option is specified.
   *
   * @param args the command line arguments to parse
   *
   * @return a CommandLine object that can be queried for command line options and their values
   */
  @SuppressWarnings({"checkstyle:regexp", "PMD.SystemPrintln"})
  public CommandLine parse(String... args) {
    try {
      DefaultParser parser = new DefaultParser();
      CommandLine parsed = parser.parse(options, args);
      if (parsed.hasOption(HELP_OPTION)) {
        printHelpAndExit();
      } else if (parsed.hasOption(VERSION_OPTION)) {
        printVersionAndExit();
      }
      return parsed;
    } catch (ParseException e) {
      System.out.println("Incorrect command line arguments: " + e.getMessage());
      printHelpAndExit();
    }
    return null; // This is OK -- will only happen in tests
  }

  /**
   * Parses a list of command line arguments. This coverts the list to an array and passes it to
   * {@link #parse(String[])}.
   *
   * @see #parse(String[])
   */
  public CommandLine parse(List<String> args) {
    return parse(args.toArray(new String[args.size()]));
  }

  /**
   * Prints a help message for the command line arguments and exits the application.
   */
  @VisibleForTesting
  void printHelpAndExit() {
    HelpFormatter hf = new HelpFormatter();
    hf.printHelp("GRIP", options);
    exit();
  }

  /**
   * Prints the app version and exits the application.
   */
  @VisibleForTesting
  @SuppressWarnings({"checkstyle:regexp", "PMD.SystemPrintln"})
  void printVersionAndExit() {
    System.out.printf(
        "GRIP version %s%n",
        CoreCommandLineHelper.class.getPackage().getImplementationVersion()
    );
    exit();
  }

  /**
   * Exits the app. This method only exists so testing can work.
   */
  @VisibleForTesting
  void exit() {
    System.exit(0);
  }

}
