package edu.wpi.grip.core.operations.network.ros;

import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.core.operations.composite.ContoursReport;
import edu.wpi.grip.core.operations.composite.LinesReport;

import com.google.common.reflect.TypeToken;

import org.ros.internal.message.Message;
import org.ros.message.MessageFactory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used to convert from a java type to a ROS message.
 */
public abstract class JavaToMessageConverter<J, M extends Message> {
  public static final JavaToMessageConverter<BlobsReport, grip_msgs.Blobs> BLOBS =
      new JavaToMessageConverter<BlobsReport, grip_msgs.Blobs>(grip_msgs.Blobs._TYPE) {
        @Override
        protected void doConvert(BlobsReport blobsReport, grip_msgs.Blobs blobsMsg, MessageFactory
            messageFactory) {
          final List<BlobsReport.Blob> reportBlobs = blobsReport.getBlobs();
          final List<grip_msgs.Blob> blobs = reportBlobs
              .stream()
              .map(blob -> {
                final grip_msgs.Blob rosBlob = messageFactory.newFromType(grip_msgs.Blob._TYPE);
                rosBlob.setSize(blob.size);
                rosBlob.setX(blob.x);
                rosBlob.setY(blob.y);
                return rosBlob;
              })
              .collect(Collectors.toList());
          blobsMsg.setBlobs(blobs);
        }
      };
  public static final JavaToMessageConverter<LinesReport, grip_msgs.Lines> LINES =
      new JavaToMessageConverter<LinesReport, grip_msgs.Lines>(grip_msgs.Lines._TYPE) {
        @Override
        protected void doConvert(LinesReport linesReport, grip_msgs.Lines linesMsg, MessageFactory
            messageFactory) {
          final List<LinesReport.Line> reportLines = linesReport.getLines();
          final List<grip_msgs.Line> lines = reportLines
              .stream()
              .map(line -> {
                final grip_msgs.Line rosLine = messageFactory.newFromType(grip_msgs.Line._TYPE);
                rosLine.setX1(line.x1);
                rosLine.setY1(line.y1);
                rosLine.setX2(line.x2);
                rosLine.setY2(line.y2);
                return rosLine;
              }).collect(Collectors.toList());
          linesMsg.setLines(lines);
        }
      };
  public static final JavaToMessageConverter<ContoursReport, grip_msgs.Contours> CONTOURS =
      new JavaToMessageConverter<ContoursReport, grip_msgs.Contours>(grip_msgs.Contours._TYPE) {
        @Override
        void doConvert(ContoursReport contoursReport, grip_msgs.Contours contoursMsg, MessageFactory
            messageFactory) {
          final List<ContoursReport.Contour> reportContours = contoursReport.getProcessedContours();
          final List<grip_msgs.Contour> contours = reportContours
              .stream()
              .map(contour -> {
                final grip_msgs.Contour rosContour = messageFactory.newFromType(grip_msgs.Contour
                    ._TYPE);
                rosContour.setArea(contour.area());
                rosContour.setCenterX(contour.centerX());
                rosContour.setCenterY(contour.centerY());
                rosContour.setHeight(contour.height());
                rosContour.setWidth(contour.width());
                rosContour.setSolidity(contour.solidity());
                return rosContour;
              }).collect(Collectors.toList());
          contoursMsg.setContours(contours);
        }
      };
  public static final JavaToMessageConverter<Boolean, std_msgs.Bool> BOOL =
      new SimpleConverter<Boolean, std_msgs.Bool>(std_msgs.Bool._TYPE,
          std_msgs.Bool::setData) {
      };
  public static final JavaToMessageConverter<Number, std_msgs.Float64> FLOAT =
      new SimpleConverter<Number, std_msgs.Float64>(
          std_msgs.Float64._TYPE,
          (msg, numb) -> msg.setData(numb.doubleValue())) {
      };
  protected final TypeToken<M> rosType;
  protected final TypeToken<J> javaType;
  private final java.lang.String type;

  private JavaToMessageConverter(String type) {
    this.type = checkNotNull(type, "type cannot be null");
    this.rosType = new TypeToken<M>(getClass()) {
    };
    this.javaType = new TypeToken<J>(getClass()) {
    };
  }

  public final java.lang.String getType() {
    return type;
  }

  /*
   * Should not be called directly
   */
  abstract void doConvert(J javaType, M message, MessageFactory messageFactory);

  /**
   * Takes a java type and a message that type maps to and adds the data to the message.
   *
   * @param javaType       The java type to put the data for the message into
   * @param message        The message to put the data into.
   * @param messageFactory Used to generate inner messages if necessary.
   */
  public void convert(J javaType, Message message, MessageFactory messageFactory) {
    checkNotNull(javaType, "The javatype cannot be null");
    checkNotNull(message, "The message cannot be null");
    @SuppressWarnings("unchecked")
    M castMessage = (M) rosType.getRawType().cast(message);
    doConvert(javaType, castMessage, messageFactory);
  }

  private abstract static class SimpleConverter<J, M extends Message> extends
      JavaToMessageConverter<J, M> {
    private final BiConsumer<M, J> messageDataAssigner;

    private SimpleConverter(String type, BiConsumer<M, J> messageDataAssigner) {
      super(type);
      this.messageDataAssigner = messageDataAssigner;
    }

    @Override
    void doConvert(J javaType, M message, MessageFactory messageFactory) {
      messageDataAssigner.accept(message, javaType);
    }
  }
}
