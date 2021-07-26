package edu.wpi.grip.annotation.processor;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.PublishableObject;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Processes elements with the GRIP annotations and generates class list files for them.
 */
@SupportedAnnotationTypes({
    "edu.wpi.grip.annotation.*",
    "com.thoughtworks.xstream.annotations.XStreamAlias"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ClassListProcessor extends AbstractProcessor {

  public static final String OPERATIONS_FILE_NAME = "operations";
  public static final String PUBLISHABLES_FILE_NAME = "publishables";
  public static final String XSTREAM_ALIASES_FILE_NAME = "xstream-aliases";

  private final Map<String, String> fileNames = makeFileNamesMap();

  private Map<String, String> makeFileNamesMap() {
    Map<String, String> map = new HashMap<>();
    map.put(Description.class.getName(), ClassListProcessor.OPERATIONS_FILE_NAME);
    map.put(PublishableObject.class.getName(), ClassListProcessor.PUBLISHABLES_FILE_NAME);
    map.put("com.thoughtworks.xstream.annotations.XStreamAlias",
        ClassListProcessor.XSTREAM_ALIASES_FILE_NAME);
    return map;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      String fileName = fileNames.get(annotation.asType().toString());
      if (fileName != null) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing " + annotation);
        createFile(fileName, classesAnnotatedWith(annotation, roundEnv));
      }
    }
    return false;
  }

  private Iterable<String> classesAnnotatedWith(TypeElement element, RoundEnvironment roundEnv) {
    return roundEnv.getElementsAnnotatedWith(element)
        .stream()
        .map(Element::asType)
        .map(t -> t.accept(TypeNameExtractor.INSTANCE, null))
        .collect(Collectors.toList());
  }

  private void createFile(String fileName, Iterable<String> classNames) {
    Filer filer = processingEnv.getFiler();

    String resource = "META-INF/" + fileName;

    try {
      FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resource);
      try (Writer writer = fileObject.openWriter()) {
        for (String className : classNames) {
          writer.write(className);
          writer.write('\n');
        }
      }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
          "Unable to create resource file " + resource + ": " + e.getMessage());
    }
  }

  private static final class TypeNameExtractor extends SimpleTypeVisitor8<String, Void> {

    public static final TypeNameExtractor INSTANCE = new TypeNameExtractor();

    @Override
    public String visitDeclared(DeclaredType t, Void o) {
      String typeName = t.toString();
      if (typeName.contains("<")) {
        typeName = typeName.substring(0, typeName.indexOf('<'));
      }
      if (t.getEnclosingType().getKind() != TypeKind.NONE) {
        // Inner class, replace '.' with '$'
        int lastDot = typeName.lastIndexOf('.');
        String first = typeName.substring(0, lastDot);
        String second = typeName.substring(lastDot + 1);
        typeName = first + "$" + second;
      }
      return typeName;
    }
  }
}
