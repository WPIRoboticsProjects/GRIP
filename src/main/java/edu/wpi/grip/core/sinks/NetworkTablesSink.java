package edu.wpi.grip.core.sinks;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Sink;
import edu.wpi.grip.core.events.SocketPublishedEvent;
import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.core.operations.composite.LinesReport;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.List;

/**
 * Allows sockets to be published onto NetworkTables
 */
public class NetworkTablesSink implements Sink {
    private static Boolean initialized = false;
    protected static final String TABLE_NAME = "GRIP";

    /**
     * The {@link NetworkTable} used by {@link NetworkTablesSink}
     */
    private static final NetworkTable table = NetworkTable.getTable(TABLE_NAME);

    public NetworkTablesSink(EventBus eventBus) {
        synchronized (NetworkTablesSink.initialized) {
            if (initialized) throw new IllegalStateException("Can not create more than one NetworkTablesSink");
            initialized = true;
        }
        eventBus.register(this);
    }


    @Subscribe
    public void onSocketPublished(SocketPublishedEvent event) {
        final OutputSocket socket = event.getSocket();
        final Object value = socket.getValue();
        final String identifier = socket.getSocketHint().getIdentifier();
        final Class type = socket.getSocketHint().getType();

        if (type == Number[].class) {
            final Number valueArray[] = (Number[]) value;
            final Double doubles[] = new Double[valueArray.length];
            checkValidSize(doubles);
            for (int i = 0; i < valueArray.length; i++) {
                doubles[i] = valueArray[i].doubleValue();
            }
            table.putNumberArray(identifier, doubles);
        } else if (type == Number.class) {
            table.putNumber(identifier, ((Number) value).doubleValue());
        } else if (type == LinesReport.class) {
            final List<LinesReport.Line> lines = ((LinesReport) value).getLines();
            final Double lineValues[] = new Double[lines.size() * 4];
            checkValidSize(lineValues);
            for (int i = 0; i < lines.size(); i++) {
                LinesReport.Line line = lines.get(i);
                lineValues[i * 4] = line.x1;
                lineValues[i * 4 + 1] = line.y1;
                lineValues[i * 4 + 2] = line.x2;
                lineValues[i * 4 + 3] = line.y2;
            }
            table.putNumberArray(identifier, lineValues);
        } else if (type == BlobsReport.Blob.class) {
            final List<BlobsReport.Blob> blobs = ((BlobsReport) value).getBlobs();
            final Double blobValues[] = new Double[blobs.size() * 3];
            checkValidSize(blobValues);
            for (int i = 0; i < blobs.size(); i++) {
                BlobsReport.Blob blob = blobs.get(i);
                blobValues[i * 3] = blob.x;
                blobValues[i * 3 + 1] = blob.y;
                blobValues[i * 3 + 2] = blob.size;
            }
            table.putNumberArray(identifier, blobValues);
        } else if (type == Size.class) {
            final Size size = (Size) value;
            table.putNumberArray(identifier, new double[]{(double) size.width(), (double) size.height()});
        } else if (type == Point.class) {
            final Point point = (Point) value;
            table.putNumberArray(identifier, new double[]{(double) point.x(), (double) point.y()});
        } else {
            table.putValue(identifier, value);
        }
        // TODO: Handle error non-fatally
    }

    /**
     * @param array The array to determine if it will fit onto network tables
     * @throws IllegalArgumentException When the array will not fit in network tables
     */
    private static void checkValidSize(Object[] array) throws IllegalArgumentException {
        if (array.length > 255) {
            throw new IllegalArgumentException("Network tables only supports arrays smaller than 255 elements");
        }
    }

    public static Boolean isInitialized(){
        return initialized;
    }

}
