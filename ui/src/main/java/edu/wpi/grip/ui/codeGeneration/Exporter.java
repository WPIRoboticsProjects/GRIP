package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.ui.codegeneration.data.TPipeline;

import com.google.inject.Singleton;

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

/**
 * Primary class for creating files and setting up code generation.
 */
@Singleton
public class Exporter {
  private static String PIPELINE_TEMPLATE = "Pipeline.vm";
  private static String PIPELINE_HTEMPLATE = "Pipeline.h.vm";
  private static String PIPELINE_HNAME = "/Pipeline.h";

  /**
   * Creates a TPipeline from the current pipeline and generates code to the target location
   *
   * @param pipeline The current pipeline that will be exported
   * @param lang     The language that will be exported into
   * @param dir      The location that the generated code will be placed
   * @param loadLib  Should be true when not in testing. Only false if in testing.
   */
  public void export(Pipeline pipeline, Language lang, File dir, boolean loadLib) {
    TPipeline tPipeline = new TPipeline(pipeline);
    TemplateMethods tempMeth = TemplateMethods.get(lang);
    VelocityContext context = new VelocityContext();
    context.put("pipeline", tPipeline);
    context.put("tMeth", tempMeth);
    context.put("fileName", dir.getName().substring(0, dir.getName().lastIndexOf(".")));
    context.put("loadLib", loadLib);
    StringBuilder templateDirBuilder = new StringBuilder();
    templateDirBuilder.append("src/main/resources/edu/wpi/grip/ui/templates/");
    templateDirBuilder.append(lang.filePath);
    templateDirBuilder.append("/");
    final String templateDir = templateDirBuilder.toString();
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
      String missingOperation = error.substring(error.lastIndexOf("/") + 1, error.lastIndexOf("."));
      throw new UnsupportedOperationException("The operation " + missingOperation 
        + " is not supported for export to " + lang);
    }
  }


  /**
   * Creates a file and generates code in it using templates.
   *
   * @param ve          The velocity engine used with the desired properties.
   * @param templateDir The directory of the velocity templates that will be used.
   * @param file        The location to put the file.
   * @param context     The velocity context including the java files that will be used by the
   *                    templates.
   */
  private void generateCode(VelocityEngine ve, String templateDir, File file,
                            VelocityContext context) {
    Template tm = ve.getTemplate(templateDir + PIPELINE_TEMPLATE);
    StringWriter sw = new StringWriter();
    tm.merge(context, sw);
    try (PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8")) {
      writer.println(sw);
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Code to generate the .h file if the export type is c++
   *
   * @param ve          The velocity engine used with the desired properties.
   * @param templateDir The directory of the velocity templates that will be used.
   * @param file        The location to put the file.
   * @param context     The velocity context including the java files that will be used by the
   *                    templates.
   */
  private void generateH(VelocityEngine ve, String templateDir, File file, VelocityContext
      context) {
    Template tm = ve.getTemplate(templateDir + PIPELINE_HTEMPLATE);
    StringWriter sw = new StringWriter();
    tm.merge(context, sw);
    try (PrintWriter writer = new PrintWriter(file.getParentFile().getAbsolutePath() 
      + File.separator  + file.getName().replace(".cpp", ".h"), "UTF-8")) {
      writer.println(sw);
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
