package edu.wpi.gripgenerator;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class FileParserTest {

    @Test
    public void testReplaceRegexParamAtEnd() {
        final String testMethod = "bilateralFilter( @ByVal Mat src, @ByVal Mat dst, int d,\n" +
                "                                   double sigmaColor, double sigmaSpace,\n" +
                "                                   int borderType/*=cv::BORDER_DEFAULT*/ );";
        final String result = "bilateralFilter( @ByVal Mat src, @ByVal Mat dst, int d,\n" +
                "                                   double sigmaColor, double sigmaSpace,\n" +
                "                                   /*=cv::BORDER_DEFAULT*/int borderType );";
        String replacedMethod = testMethod.replaceAll(FileParser.methodReorderPattern, FileParser.methodNewOrder);
        assertEquals("Result was not the expected string after regex replacement", result, replacedMethod);
    }

    @Test
    public void testReplaceRegexParamAtMiddleAndEnd() {
        final String testMethod = "GaussianBlur( @ByVal Mat src, @ByVal Mat dst, @ByVal Size ksize,\n" +
                "                                double sigmaX, double sigmaY/*=0*/,\n" +
                "                                int borderType/*=cv::BORDER_DEFAULT*/ );";
        final String result = "GaussianBlur( @ByVal Mat src, @ByVal Mat dst, @ByVal Size ksize,\n" +
                "                                double sigmaX, /*=0*/double sigmaY,\n" +
                "                                /*=cv::BORDER_DEFAULT*/int borderType );";
        String replacedMethod = testMethod.replaceAll(FileParser.methodReorderPattern, FileParser.methodNewOrder);
        assertEquals("Result was not the expected string after regex replacement", result, replacedMethod);
    }

    @Test
    public void testSimpleRearrange() {
        final String testString = "CvPoint2D32f c/*=CvPoint2D32f()*/,";
        final String expectedResult = "/*=CvPoint2D32f()*/CvPoint2D32f c,";
        String replacedMethod = testString.replaceAll(FileParser.methodReorderPattern, FileParser.methodNewOrder);
        assertEquals("Result was not the expected string after regex replacement", expectedResult, replacedMethod);
    }

    @Test
    public void testNegativeValues(){
        final String testMethod = "multiply(@ByVal Mat src1, @ByVal Mat src2,\n" +
                "                           @ByVal Mat dst, double scale/*=1*/, int dtype/*=-1*/);";
        final String expectedResult = "multiply(@ByVal Mat src1, @ByVal Mat src2,\n" +
                "                           @ByVal Mat dst, /*=1*/double scale, /*=-1*/int dtype);";
        String replacedMethod = testMethod.replaceAll(FileParser.methodReorderPattern, FileParser.methodNewOrder);
        assertEquals("Result was not the expected string after regex replacement", expectedResult, replacedMethod);
    }
}