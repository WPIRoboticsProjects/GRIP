package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum ThresholdTypesEnum {

    /** \f[\texttt{dst} (x,y) =  \fork{\texttt{maxval}}{if \(\texttt{src}(x,y) &gt; \texttt{thresh}\)}{0}{otherwise}\f] */
    THRESH_BINARY(opencv_imgproc.THRESH_BINARY), /** \f[\texttt{dst} (x,y) =  \fork{0}{if \(\texttt{src}(x,y) &gt; \texttt{thresh}\)}{\texttt{maxval}}{otherwise}\f] */
    THRESH_BINARY_INV(opencv_imgproc.THRESH_BINARY_INV), /** \f[\texttt{dst} (x,y) =  \fork{\texttt{threshold}}{if \(\texttt{src}(x,y) &gt; \texttt{thresh}\)}{\texttt{src}(x,y)}{otherwise}\f] */
    THRESH_TRUNC(opencv_imgproc.THRESH_TRUNC), /** \f[\texttt{dst} (x,y) =  \fork{\texttt{src}(x,y)}{if \(\texttt{src}(x,y) &gt; \texttt{thresh}\)}{0}{otherwise}\f] */
    THRESH_TOZERO(opencv_imgproc.THRESH_TOZERO), /** \f[\texttt{dst} (x,y) =  \fork{0}{if \(\texttt{src}(x,y) &gt; \texttt{thresh}\)}{\texttt{src}(x,y)}{otherwise}\f] */
    THRESH_TOZERO_INV(opencv_imgproc.THRESH_TOZERO_INV), THRESH_MASK(opencv_imgproc.THRESH_MASK), /** flag, use Otsu algorithm to choose the optimal threshold value */
    THRESH_OTSU(opencv_imgproc.THRESH_OTSU), /** flag, use Triangle algorithm to choose the optimal threshold value */
    THRESH_TRIANGLE(opencv_imgproc.THRESH_TRIANGLE);

    public final int value;

    ThresholdTypesEnum(int value) {
        this.value = value;
    }
}
