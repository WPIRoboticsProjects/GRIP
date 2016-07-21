package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Step;
import edu.wpi.grip.ui.codegeneration.data.TPipeline;

import com.google.common.collect.ImmutableList;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Primary class for creating files and setting up code generation.
 */
public class Exporter implements Runnable {
  private static final Logger logger = Logger.getLogger(Exporter.class.getName());
  private static final String PIPELINE_TEMPLATE = "Pipeline.vm";
  private static final String PIPELINE_HTEMPLATE = "Pipeline.h.vm";
  private final ImmutableList<Step> steps;
  private final Language lang;
  private final File dir;
  private final boolean notTesting;

  /**
   * Constructor for an exporter.
   * 
   * @param steps an Immutable List of the steps in the pipeline to generate.
   * @param lang the language to generate code for.
   * @param dir the file to generate the main code to.
   * @param notTesting true when not in testing false during testing.
   */
  public Exporter(ImmutableList<Step> steps, Language lang, File dir, boolean notTesting) {
    this.steps = steps;
    this.lang = lang;
    this.dir = dir;
    this.notTesting = notTesting;
  }

  @Override
  public void run() {
    TPipeline tPipeline = new TPipeline(steps);
    TemplateMethods tempMeth = TemplateMethods.get(lang);
    VelocityContext context = new VelocityContext();
    context.put("pipeline", tPipeline);
    context.put("tMeth", tempMeth);
    context.put("fileName", dir.getName().substring(0, dir.getName().lastIndexOf(".")));
    context.put("loadLib", notTesting);
    StringBuilder templateDirBuilder = new StringBuilder(50);
    templateDirBuilder.append("src/main/resources/edu/wpi/grip/ui/templates/").append(lang.filePath)
        .append('/');
    final String templateDir = templateDirBuilder.toString().replaceAll("/", File.separator);
    VelocityEngine ve = new VelocityEngine();
    Properties props = new Properties();
    props.put("velocimacro.library", templateDir + "macros.vm");
    ve.init(props);
    try {
      generateCode(ve, templateDir, dir, context);
      if (lang.equals(Language.CPP)) {
        generateH(ve, templateDir, dir, context);
      }
    } catch (ResourceNotFoundException e) {
      String error = e.getMessage();
      String missingOperation = error.substring(error.lastIndexOf('/') + 1, error.lastIndexOf('.'));
      logger.log(Level.SEVERE,
          "The operation " + missingOperation + " is not supported for export to " + lang, e);
    }
  }

  /**
   * Creates a file and generates code in it using templates.
   *
   * @param ve The velocity engine used with the desired properties.
   * @param templateDir The directory of the velocity templates that will be used.
   * @param file The location to put the file.
   * @param context The velocity context including the java files that will be used by the
   *        templates.
   */
  private void generateCode(VelocityEngine ve, String templateDir, File file,
      VelocityContext context) {
    Template tm = ve.getTemplate(templateDir + PIPELINE_TEMPLATE);
    StringWriter sw = new StringWriter();
    tm.merge(context, sw);
    try (PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8")) {
      writer.println(sw);
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      logger.log(Level.SEVERE, "Unable to write to file", e);
    }
  }

  /**
   * Code to generate the .h file if the export type is c++
   *
   * @param ve The velocity engine used with the desired properties.
   * @param templateDir The directory of the velocity templates that will be used.
   * @param file The location to put the file.
   * @param context The velocity context including the java files that will be used by the
   *        templates.
   */
  private void generateH(VelocityEngine ve, String templateDir, File file,
      VelocityContext context) {
    Template tm = ve.getTemplate(templateDir + PIPELINE_HTEMPLATE);
    StringWriter sw = new StringWriter();
    tm.merge(context, sw);
    try (PrintWriter writer = new PrintWriter(file.getParentFile().getAbsolutePath()
        + File.separator + file.getName().replace(".cpp", ".h"), "UTF-8")) {
      writer.println(sw);
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      logger.log(Level.SEVERE, "Unable to write to file", e);
    }
  }

}
