package edu.wpi.grip.core.operations.templated;


import com.google.inject.Singleton;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.function.Supplier;

/**
 * Allows you to easily create {@link Supplier<Operation>} using only {@link SocketHint SocketHints} and a lambda that
 * will be run as part of the {@link Operation#perform()} method.
 */
@Singleton
@SuppressWarnings("PMD.GenericsNaming")
public final class TemplateFactory {
    /*
     * Intentionally package private
     */
    static final String ASSERTION_MESSAGE = "Output must be present for this operation to complete correctly.";
    private final InputSocket.Factory isf;
    private final OutputSocket.Factory osf;

    public TemplateFactory(InputSocket.Factory inputSocketFactory,
                           OutputSocket.Factory outputSocketFactory) {
        this.isf = inputSocketFactory;
        this.osf = outputSocketFactory;
    }

    public <T1, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<R> rSocketHint,
            OneSourceOneDestinationOperation.Performer<T1, R> performer) {
        return () -> new OneSourceOneDestinationOperation<>(isf, osf, performer, t1SocketHint, rSocketHint);
    }

    public <T1, T2, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<T2> t2SocketHint,
            SocketHint<R> rSocketHint,
            TwoSourceOneDestinationOperation.Performer<T1, T2, R> performer) {
        return () -> new TwoSourceOneDestinationOperation<>(isf, osf, t1SocketHint, t2SocketHint, rSocketHint, performer);
    }

    public <T1, T2, T3, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<T2> t2SocketHint,
            SocketHint<T3> t3SocketHint,
            SocketHint<R> rSocketHint,
            ThreeSourceOneDestinationOperation.Performer<T1, T2, T3, R> performer) {
        return () -> new ThreeSourceOneDestinationOperation<>(isf, osf, t1SocketHint, t2SocketHint, t3SocketHint, rSocketHint, performer);
    }

    public <T1, T2, T3, T4, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<T2> t2SocketHint,
            SocketHint<T3> t3SocketHint,
            SocketHint<T4> t4SocketHint,
            SocketHint<R> rSocketHint,
            FourSourceOneDestinationOperation.Performer<T1, T2, T3, T4, R> performer) {
        return () -> new FourSourceOneDestinationOperation<>(isf, osf, t1SocketHint, t2SocketHint, t3SocketHint, t4SocketHint, rSocketHint, performer);
    }

    public <T1, T2, T3, T4, T5, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<T2> t2SocketHint,
            SocketHint<T3> t3SocketHint,
            SocketHint<T4> t4SocketHint,
            SocketHint<T5> t5SocketHint,
            SocketHint<R> rSocketHint,
            FiveSourceOneDestinationOperation.Performer<T1, T2, T3, T4, T5, R> performer) {
        return () -> new FiveSourceOneDestinationOperation<>(isf, osf, t1SocketHint, t2SocketHint, t3SocketHint, t4SocketHint, t5SocketHint, rSocketHint, performer);
    }

    public <T1, T2, T3, T4, T5, T6, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<T2> t2SocketHint,
            SocketHint<T3> t3SocketHint,
            SocketHint<T4> t4SocketHint,
            SocketHint<T5> t5SocketHint,
            SocketHint<T6> t6SocketHint,
            SocketHint<R> rSocketHint,
            SixSourceOneDestinationOperation.Performer<T1, T2, T3, T4, T5, T6, R> performer) {
        return () -> new SixSourceOneDestinationOperation<>(isf, osf, t1SocketHint, t2SocketHint, t3SocketHint, t4SocketHint, t5SocketHint, t6SocketHint, rSocketHint, performer);
    }

    public <T1, T2, T3, T4, T5, T6, T7, R> Supplier<Operation> create(
            SocketHint<T1> t1SocketHint,
            SocketHint<T2> t2SocketHint,
            SocketHint<T3> t3SocketHint,
            SocketHint<T4> t4SocketHint,
            SocketHint<T5> t5SocketHint,
            SocketHint<T6> t6SocketHint,
            SocketHint<T7> t7SocketHint,
            SocketHint<R> rSocketHint,
            SevenSourceOneDestinationOperation.Performer<T1, T2, T3, T4, T5, T6, T7, R> performer) {
        return () -> new SevenSourceOneDestinationOperation<>(isf, osf, t1SocketHint, t2SocketHint, t3SocketHint, t4SocketHint, t5SocketHint, t6SocketHint, t7SocketHint, rSocketHint, performer);
    }

    public Supplier<Operation> createAllMatTwoSource(
            SocketHint<Mat> matSocketHint,
            SocketHint<Mat> matSocketHint2,
            SocketHint<Mat> matSocketHint3,
            TwoSourceOneDestinationOperation.Performer<Mat, Mat, Mat> performer) {
        return create(matSocketHint, matSocketHint2, matSocketHint3, performer);
    }

    public Supplier<Operation> createAllMatTwoSource(TwoSourceOneDestinationOperation.Performer<Mat, Mat, Mat> performer) {
        return createAllMatTwoSource(srcSocketHint(Mat.class, 1), srcSocketHint(Mat.class, 2), dstMatSocketHint(), performer);
    }

    public Supplier<Operation> createAllMatOneSource(
            SocketHint<Mat> matSocketHint,
            SocketHint<Mat> matSocketHint2,
            OneSourceOneDestinationOperation.Performer<Mat, Mat> performer) {
        return create(matSocketHint, matSocketHint2, performer);
    }

    public Supplier<Operation> createAllMatOneSource(OneSourceOneDestinationOperation.Performer<Mat, Mat> performer) {
        return createAllMatOneSource(srcSocketHint(Mat.class, 1), dstMatSocketHint(), performer);
    }


    private <R> SocketHint<R> srcSocketHint(Class<R> srcType, int index) {
        return new SocketHint.Builder<>(srcType).identifier("src" + index).build();
    }

    private SocketHint<Mat> dstMatSocketHint() {
        return SocketHints.Outputs.createMatSocketHint("dst");
    }
}
