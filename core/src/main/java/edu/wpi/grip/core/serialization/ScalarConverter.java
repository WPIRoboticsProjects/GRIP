package edu.wpi.grip.core.serialization;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.bytedeco.javacpp.opencv_core.Scalar;

public class ScalarConverter implements Converter {
    private static final String RED = "RED", GREEN = "GREEN", BLUE = "BLUE";
    private final XStream xstream;

    protected ScalarConverter(XStream xstream) {
        this.xstream = xstream;
    }


    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Scalar scalar = (Scalar) obj;
        writer.startNode(xstream.getMapper().serializedClass(Scalar.class));
        writer.addAttribute(RED, Double.toString(scalar.red()));
        writer.addAttribute(GREEN, Double.toString(scalar.green()));
        writer.addAttribute(BLUE, Double.toString(scalar.blue()));
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveDown();
        double red = Double.parseDouble(reader.getAttribute(RED));
        double green = Double.parseDouble(reader.getAttribute(GREEN));
        double blue = Double.parseDouble(reader.getAttribute(BLUE));
        reader.moveUp();
        return new Scalar().red(red).green(green).blue(blue);
    }

    @Override
    public boolean canConvert(Class type) {
        return Scalar.class.equals(type);
    }
}
