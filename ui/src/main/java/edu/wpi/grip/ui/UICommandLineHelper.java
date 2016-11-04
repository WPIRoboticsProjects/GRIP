package edu.wpi.grip.ui;

import edu.wpi.grip.core.CoreCommandLineHelper;

import org.apache.commons.cli.Option;

/**
 * Command line helper for the UI. This has all the options of the {@link CoreCommandLineHelper}.
 */
public class UICommandLineHelper extends CoreCommandLineHelper {

  public static final String HEADLESS_OPTION = "headless";

  private static final Option headlessOption =
      Option.builder()
          .longOpt(HEADLESS_OPTION)
          .desc("Run in headless mode")
          .build();

  public UICommandLineHelper() {
    super(headlessOption);
  }

}
