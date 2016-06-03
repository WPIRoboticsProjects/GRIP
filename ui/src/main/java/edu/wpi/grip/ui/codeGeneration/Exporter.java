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
import java.util.Properties;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.java.TPipeline;

@Singleton
public class Exporter {
  static{
	Properties props = new Properties();
	props.put("velocimacro.library", "src/main/resources/edu/wpi/grip/ui/templates/java/macros.vm");
    Velocity.init(props);
  }

  public String stepNames(Pipeline pipeline) {
    StringBuilder out = new StringBuilder();
    for (Step step : getSteps(pipeline)) {
      out.append(step.getOperationDescription().name() + " \n");
      out.append(getInputNames(step));
      out.append(getOutputNames(step));
    }
    return out.toString();
  }

  public List<Step> getSteps(Pipeline pipeline) {
    return pipeline.getSteps();
  }

  public String getInputNames(Step step) {
    StringBuilder out = new StringBuilder();
    for (InputSocket input : step.getInputSockets()) {
      String type = TemplateMethods.parseSocketType(input);
      String name = TemplateMethods.parseSocketName(input);
      String value = TemplateMethods.parseSocketValue(input);
      out.append(createClass(type, name, value) +  " \n");
      out.append(type + "\n");
    }
    out.append(step.getOperationDescription().summary() + "\n");
    return out.toString();
  }

  public static String createClass(String type, String name, String value) {
    if(type.contains("Enum")){
      return "int " + name + " = Imgproc." + value;
    }
    else if(type.equals("Type")){
      return "String " + name + " = \"" + value + "\"";
    }
    else if(type.equals("List")){
      StringBuilder out = new StringBuilder();
      out.append("double min" + name +" = "+ value.substring(1,value.indexOf(","))+ "\n");
      out.append("double max" + name +" = "+ value.substring(value.indexOf(",")+1,value.lastIndexOf
          ("]")));
      return out.toString();
    }
    else{
      return type + " " + name + " = " + value;
    }

  }



  public String getOutputNames(Step step) {
    StringBuilder out = new StringBuilder();
    for (OutputSocket output : step.getOutputSockets()) {
      out.append("    " + output.getSocketHint().getType().getSimpleName() + ": " + output.getSocketHint()
          .getIdentifier() +
          " \n");
    }
    return out.toString();
  }

  public void export(Pipeline pipeline){
    TPipeline tPipeline = new TPipeline(pipeline);
    VelocityContext context = new VelocityContext();
    context.put("pipeline", tPipeline);
    String template = "src/main/resources/edu/wpi/grip/ui/templates/java/Pipeline.vm";
    Template tm = Velocity.getTemplate(template);

    StringWriter sw = new StringWriter();
    tm.merge(context, sw);
    try (PrintWriter writer = new PrintWriter("PipelineTest.java", "UTF-8")){
      	writer.println(sw);
    	}catch(UnsupportedEncodingException | FileNotFoundException e){
    		
    	}
  }


}
