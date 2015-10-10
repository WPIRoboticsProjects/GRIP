package templates;

import edu.wpi.gripgenerator.templates.SocketHintDeclaration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SocketHintDeclarationTest {
    SocketHintDeclaration testDeclaration = new SocketHintDeclaration("Mat", Arrays.asList("src1", "src2"), true);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetDeclaration() {
        final String outputString = "private final SocketHint<Mat> src1Hint = new SocketHint<Mat>(\"src1\", Mat.class), src2Hint = new SocketHint<Mat>(\"src2\", Mat.class);";
        assertEquals(outputString, testDeclaration.getDeclaration().toString());
    }
}