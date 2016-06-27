package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.Pipeline;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.ClassPath;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Helper for saving and loading a processing pipeline to and from a file.
 */
@Singleton
public class Project {

  protected final XStream xstream = new XStream(new PureJavaReflectionProvider());
  @Inject
  private Pipeline pipeline;
  private Optional<File> file = Optional.empty();

  @Inject
  public void initialize(StepConverter stepConverter,
                         SourceConverter sourceConverter,
                         SocketConverter socketConverter,
                         ConnectionConverter connectionConverter,
                         ProjectSettingsConverter projectSettingsConverter) {
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.registerConverter(stepConverter);
    xstream.registerConverter(sourceConverter);
    xstream.registerConverter(socketConverter);
    xstream.registerConverter(connectionConverter);
    xstream.registerConverter(projectSettingsConverter);
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

  @VisibleForTesting
  void open(Reader reader) {
    this.pipeline.clear();
    this.xstream.fromXML(reader);
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
    this.xstream.toXML(this.pipeline, writer);
  }
}
