package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.VersionManager;
import edu.wpi.grip.core.events.DirtiesSaveEvent;
import edu.wpi.grip.core.events.WarningEvent;
import edu.wpi.grip.core.exception.InvalidSaveException;
import edu.wpi.grip.core.exception.UnknownSaveFormatException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.StreamException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Helper for saving and loading a processing pipeline to and from a file.
 */
@Singleton
public class Project {

  protected final XStream xstream = new XStream(new PureJavaReflectionProvider());
  @Inject
  private EventBus eventBus;
  @Inject
  private Pipeline pipeline;
  private ProjectModel model;
  @Inject
  private PipelineRunner pipelineRunner;
  private Optional<File> file = Optional.empty();
  private final ObservableBoolean saveIsDirty = new ObservableBoolean();

  @Inject
  public void initialize(ProjectConverter projectConverter,
                         StepConverter stepConverter,
                         SourceConverter sourceConverter,
                         SocketConverter socketConverter,
                         ConnectionConverter connectionConverter,
                         ProjectSettingsConverter projectSettingsConverter,
                         CodeGenerationSettingsConverter codeGenerationSettingsConverter,
                         VersionConverter versionConverter) {
    model = new ProjectModel(pipeline, VersionManager.CURRENT_VERSION);
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.registerConverter(projectConverter);
    xstream.ignoreUnknownElements(); // ignores all unknown tags
    xstream.registerConverter(stepConverter);
    xstream.registerConverter(sourceConverter);
    xstream.registerConverter(socketConverter);
    xstream.registerConverter(connectionConverter);
    xstream.registerConverter(projectSettingsConverter);
    xstream.registerConverter(versionConverter);
    xstream.registerConverter(codeGenerationSettingsConverter);
    try {
      ClassPath cp = ClassPath.from(getClass().getClassLoader());
      cp.getAllClasses()
          .stream()
          .filter(ci -> ci.getPackageName().startsWith("edu.wpi.grip"))
          .map(ClassPath.ClassInfo::load)
          .filter(clazz -> clazz.isAnnotationPresent(XStreamAlias.class))
          .forEach(clazz -> {
            try {
              xstream.processAnnotations(clazz);
            } catch (InternalError ex) {
              throw new AssertionError("Failed to load class: " + clazz.getName(), ex);
            }

          });
    } catch (IOException ex) {
      throw new AssertionError("Could not load classes for XStream annotation processing", ex);
    }
  }

  /**
   * @return The file that this project is located in, if it was loaded from/saved to a file.
   */
  public Optional<File> getFile() {
    return file;
  }

  public void setFile(Optional<File> file) {
    this.file = file;
  }

  /**
   * Load the project from a file.
   */
  public void open(File file) throws IOException {
    try (final InputStreamReader reader = new InputStreamReader(new FileInputStream(file),
        StandardCharsets.UTF_8)) {
      this.open(reader);
    }
    this.file = Optional.of(file);
  }

  /**
   * Loads the project defined by the given XML string. This is intended to be used to be able to
   * programmatically run a pipeline from a remote source. Therefore, this does <strong>not</strong>
   * save the contents to disk; if it is called in GUI mode, the user will have to manually save the
   * file.
   *
   * @param projectXml the XML string defining the project to open
   */
  public void open(String projectXml) {
    open(new StringReader(projectXml));
  }

  @VisibleForTesting
  void open(Reader reader) throws InvalidSaveException {
    pipelineRunner.stopAndAwait();
    this.pipeline.clear();
    try {
      Object loaded = xstream.fromXML(reader);
      if (loaded instanceof Pipeline) {
        // Unversioned pre-2.0.0 save.
        // It's compatible with the current version because it loaded without exceptions.
        // When saved, the old save file will be upgraded to the current version.
        model = new ProjectModel(pipeline, VersionManager.CURRENT_VERSION);
      } else if (loaded instanceof ProjectModel) {
        // Version 2.0.0 or above. Since we got to this point, we know it's compatible
        // (otherwise a ConversionException would have been thrown).
        // Saves from different versions of GRIP will be upgraded/downgraded to the current version
        model = new ProjectModel(pipeline, VersionManager.CURRENT_VERSION);
      } else {
        // Uhh... probably a future version
        throw new UnknownSaveFormatException(
            String.format("Unknown save format (loaded a %s)", loaded.getClass().getName())
        );
      }
    } catch (ConversionException e) {
      // Incompatible save, or a bug with de/serialization
      throw new InvalidSaveException("There are incompatible sources or steps in the pipeline", e);
    } catch (StreamException e) {
      // Invalid XML
      throw new InvalidSaveException("Invalid XML", e);
    }
    pipelineRunner.startAsync();
    saveIsDirty.set(false);
  }

  /**
   * Tries to save this project to the given file. Unlike {@link #save(File)}, this will <i>not</i>
   * throw an IOException and will instead post a warning event to the event bus.
   *
   * @param file the file to save to
   *
   * @return true if the project was successfully saved to the given file, or false if the file
   *              could not be written to
   */
  public boolean trySave(File file) {
    try {
      save(file);
      return true;
    } catch (IOException e) {
      eventBus.post(new WarningEvent(
          "Could not save project",
          "The project could not be saved to " + file.getAbsolutePath()
              + ".\n\nCause: " + e.getMessage()
      ));
      return false;
    }
  }

  /**
   * Save the project to a file.
   */
  public void save(File file) throws IOException {
    try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file),
        StandardCharsets.UTF_8)) {
      this.save(writer);
    }
    this.file = Optional.of(file);
  }

  public void save(Writer writer) {
    this.xstream.toXML(model, writer);
    saveIsDirty.set(false);
  }

  public boolean isSaveDirty() {
    return saveIsDirty.get();
  }

  public void addIsSaveDirtyConsumer(Consumer<Boolean> consumer) {
    saveIsDirty.addConsumer(consumer);
  }

  @Subscribe
  public void onDirtiesSaveEvent(DirtiesSaveEvent dirtySaveEvent) {
    // Only update the flag the save isn't already dirty
    // We don't need to be redundantly checking if the event dirties the save
    if (!saveIsDirty.get() && dirtySaveEvent.doesDirtySave()) {
      saveIsDirty.set(true);
    }
  }

  private static final class ObservableBoolean {

    private boolean b = false;
    private final List<Consumer<Boolean>> consumers = new LinkedList<>();

    public void set(boolean b) {
      this.b = b;
      consumers.parallelStream().forEach(c -> c.accept(b));
    }

    public boolean get() {
      return b;
    }

    public void addConsumer(Consumer<Boolean> consumer) {
      consumers.add(consumer);
    }
  }
}
