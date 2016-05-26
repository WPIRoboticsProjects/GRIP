package edu.wpi.grip.ui.codegeneration;


import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

/**
 * Created by Toby on 5/24/16.
 */
@Singleton
public class Exporter {

  TemplateMethods tempMeth;
  @Inject
  public Exporter(Pipeline pipeline) {
    Velocity.init();
  }

  public String stepNames(Pipeline pipeline) {
    String out = "";
    for (Step step : getSteps(pipeline)) {
      out += step.getOperationDescription().name() + " \n";
      out += getInputNames(step);
      out += getOutputNames(step);
    }
    return out;
  }

  public static void printToFile(String string) {
    PrintWriter writer;
    try {
      writer = new PrintWriter("PipelineTest.java", "UTF-8");
      writer.println(string);
      writer.close();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public List<Step> getSteps(Pipeline pipeline) {
    return pipeline.getSteps();
  }

  public String getInputNames(Step step) {
    String out = "";
    for (InputSocket input : step.getInputSockets()) {
      String type = tempMeth.parseSocketType(input);
      String name = tempMeth.parseSocketName(input);
      String value = tempMeth.parseSocketValue(input);
      out += createClass(type, name, value) +  " \n";
      out += input.getConnections() + "\n";
    }
    out += step.getOperationDescription().summary() + "\n";
    return out;
  }

  public static String createClass(String type, String name, String value) {
    String out = "";
    if(type.contains("Enum")){
      return "int " + name + " = Imgproc." + value;
    }
    else if(type.equals("Type")){
      return "String " + name + " = \"" + value + "\"";
    }
    else if(type.equals("List")){
      out += "double min" + name +" = "+ value.substring(1,value.indexOf(","))+ "\n";
      out += "double max" + name +" = "+ value.substring(value.indexOf(",")+1,value.lastIndexOf
          ("]"));
      return out;
    }
    else{
      return type + " " + name + " = " + value;
    }

  }



  public String getOutputNames(Step step) {
    String out = "";
    for (OutputSocket output : step.getOutputSockets()) {
      out += "    " + output.getSocketHint().getType().getSimpleName() + ": " + output.getSocketHint()
          .getIdentifier() +
          " \n";
    }
    return out;
  }

  public void export(Pipeline pipeline) {
    tempMeth = new TemplateMethods();
    VelocityContext context = new VelocityContext();
    context.put("pipeline", pipeline);
    tempMeth.setPipeline(pipeline);
    context.put("tempMeth",tempMeth);
    String template = "src/main/resources/edu/wpi/grip/ui/templates/Pipeline.vm";
    Template tm = Velocity.getTemplate(template);

    StringWriter sw = new StringWriter();
    tm.merge(context, sw);
    System.out.println(sw);
    PrintWriter writer;
    try {
      writer = new PrintWriter("PipelineTest.java", "UTF-8");
      writer.println(sw);
      writer.close();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }


  }


}
