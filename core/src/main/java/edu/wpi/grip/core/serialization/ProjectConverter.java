package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.VersionManager;

import com.github.zafarkhaja.semver.Version;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ProjectConverter implements Converter {

  private static final String VERSION_ATTRIBUTE = "version";

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    ProjectModel model = (ProjectModel) source;
    writer.addAttribute(VERSION_ATTRIBUTE, model.getVersion().toString());
    writer.startNode("grip:Pipeline");
    context.convertAnother(model.getPipeline());
    writer.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String v = reader.getAttribute(VERSION_ATTRIBUTE);
    Version version = v == null ? VersionManager.LAST_UNVERSIONED_RELEASE : Version.valueOf(v);
    reader.moveDown();
    Pipeline pipeline = (Pipeline) context.convertAnother(null, Pipeline.class);
    reader.moveUp();
    return new ProjectModel(pipeline, version);
  }

  @Override
  public boolean canConvert(Class type) {
    return type.equals(ProjectModel.class);
  }
}
