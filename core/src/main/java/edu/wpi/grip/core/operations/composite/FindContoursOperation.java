package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * An {@link Operation} that, given a binary image, produces a list of contours of all of the shapes in the image
 */
public class FindContoursOperation implements Operation {

    private final SocketHint<Mat> inputHint =
            new SocketHint.Builder<>(Mat.class).identifier("Input").build();

    private final SocketHint<Boolean> externalHint =
            SocketHints.createBooleanSocketHint("External Only", false);

    private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport.class)
            .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

    @Override
    public String getName() {
        return "Find Contours";
    }

    @Override
    public String getDescription() {
        return "Detect contours in a binary image.";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/find-contours.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, externalHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, contoursHint)};
    }

    @Override
    public Optional<?> createData() {
        return Optional.of(new Mat());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final Mat tmp = ((Optional<Mat>) data).get();
        final boolean externalOnly = ((InputSocket<Boolean>) inputs[1]).getValue().get();
        final OutputSocket<ContoursReport> contoursSocket = (OutputSocket<ContoursReport>) outputs[0];
        final ContoursReport contours = contoursSocket.getValue().get();

        if (input.empty()) {
            return;
        }

        // findContours modifies its input, so we pass it a temporary copy of the input image
        input.copyTo(tmp);

        // OpenCV has a few different things it can return from findContours, but for now we only use EXTERNAL and LIST.
        // The other ones involve hierarchies of contours, which might be useful in some situations, but probably only
        // when processing the contours manually in code (so, not in a graphical pipeline).
        findContours(tmp, contours.getContours(), externalOnly ? CV_RETR_EXTERNAL : CV_RETR_LIST,
                CV_CHAIN_APPROX_TC89_KCOS);

        contours.setRows(input.rows());
        contours.setCols(input.cols());
        contoursSocket.setValue(contours);
    }
}
