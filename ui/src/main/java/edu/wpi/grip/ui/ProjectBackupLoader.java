package edu.wpi.grip.ui;

import edu.wpi.grip.core.GripFileManager;
import edu.wpi.grip.core.events.DirtiesSaveEvent;
import edu.wpi.grip.core.serialization.Project;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStreamException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class handling loading project backups when the app is launched. This improves the UX by letting
 * users resume where they left off.
 */
@Singleton
public class ProjectBackupLoader {

  private static final Logger logger = Logger.getLogger(ProjectBackupLoader.class.getName());

  @Inject
  private EventBus eventBus;
  @Inject
  private Project project;

  /**
   * Loads the backup project, or the previous save if it's identical to the backup. If there was
   * no previous save, the project will have no file associated with it. Does nothing if the backup
   * file does not exist.
   */
  public void loadBackupOrPreviousSave() {
    if (GripFileManager.LAST_SAVE_FILE.exists()) {
      // Load whichever is readable and more recent, backup or previous save
      try {
        File lastSaveFile = lastSaveFile();
        if (lastSaveFile != null) {
          // Compare last save to backup
          String backupText = readFileText(GripFileManager.BACKUP_FILE);
          String lastSaveText = readFileText(lastSaveFile);
          if (backupText.equals(lastSaveText) || !GripFileManager.BACKUP_FILE.exists()) {
            // No point in loading the backup since it's identical to the last save
            logger.info("Loading the last save file");
            project.open(lastSaveFile);
          } else if (GripFileManager.BACKUP_FILE.exists()) {
            // Load backup, set the file to the last save file (instead of the backup),
            // and post an event marking the save as dirty
            loadBackup();
            project.setFile(Optional.of(lastSaveFile));
            eventBus.post(DirtiesSaveEvent.DIRTIES_SAVE_EVENT);
          }
        } else if (GripFileManager.BACKUP_FILE.exists()) {
          // Couldn't read from the last save, just load the backup if possible
          loadBackup();
          project.setFile(Optional.empty());
        }
      } catch (XStreamException | IOException e) {
        logger.log(Level.WARNING, "Could not open the last project file", e);
      }
    } else if (GripFileManager.BACKUP_FILE.exists()) {
      // Load the backup, if possible
      loadBackup();
      project.setFile(Optional.empty());
    }
  }

  private File lastSaveFile() throws IOException {
    List<String> lines = Files.readAllLines(GripFileManager.LAST_SAVE_FILE.toPath());
    if (lines.size() == 1) {
      return new File(lines.get(0));
    } else {
      logger.warning("Unexpected data in last_save file: " + lines);
      return null;
    }
  }

  private void loadBackup() {
    try {
      logger.info("Loading backup file");
      project.open(GripFileManager.BACKUP_FILE);
    } catch (XStreamException | IOException e) {
      logger.log(Level.WARNING, "Could not load backup file", e);
    }
    eventBus.post(DirtiesSaveEvent.DIRTIES_SAVE_EVENT);
  }

  /**
   * Reads all the text from the given file into a single string.
   */
  private static String readFileText(File file) throws IOException {
    return StringUtils.join(Files.readAllLines(file.toPath()), '\n');
  }

}


