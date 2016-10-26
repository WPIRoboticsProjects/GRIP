package edu.wpi.grip.core.serialization;

import com.github.zafarkhaja.semver.Version;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for {@link Version GRIP verisons}.
 */
public class VersionConverter implements Converter {

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Version v = (Version) source;
    writer.setValue(v.toString());
  }

  @Override
  public Version unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return Version.valueOf(reader.getValue());
  }

  @Override
  public boolean canConvert(Class type) {
    return Version.class.isAssignableFrom(type);
  }

}
