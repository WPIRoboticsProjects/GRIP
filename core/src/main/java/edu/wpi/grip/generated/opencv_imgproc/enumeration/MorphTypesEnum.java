package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum MorphTypesEnum {

    /** see cv::erode */
    MORPH_ERODE(opencv_imgproc.MORPH_ERODE), /** see cv::dilate */
    MORPH_DILATE(opencv_imgproc.MORPH_DILATE), /** an opening operation
 *  \f[\texttt{dst} = \mathrm{open} ( \texttt{src} , \texttt{element} )= \mathrm{dilate} ( \mathrm{erode} ( \texttt{src} , \texttt{element} ))\f] */
    MORPH_OPEN(opencv_imgproc.MORPH_OPEN), /** a closing operation
 *  \f[\texttt{dst} = \mathrm{close} ( \texttt{src} , \texttt{element} )= \mathrm{erode} ( \mathrm{dilate} ( \texttt{src} , \texttt{element} ))\f] */
    MORPH_CLOSE(opencv_imgproc.MORPH_CLOSE), /** a morphological gradient
 *  \f[\texttt{dst} = \mathrm{morph\_grad} ( \texttt{src} , \texttt{element} )= \mathrm{dilate} ( \texttt{src} , \texttt{element} )- \mathrm{erode} ( \texttt{src} , \texttt{element} )\f] */
    MORPH_GRADIENT(opencv_imgproc.MORPH_GRADIENT), /** "top hat"
 *  \f[\texttt{dst} = \mathrm{tophat} ( \texttt{src} , \texttt{element} )= \texttt{src} - \mathrm{open} ( \texttt{src} , \texttt{element} )\f] */
    MORPH_TOPHAT(opencv_imgproc.MORPH_TOPHAT), /** "black hat"
 *  \f[\texttt{dst} = \mathrm{blackhat} ( \texttt{src} , \texttt{element} )= \mathrm{close} ( \texttt{src} , \texttt{element} )- \texttt{src}\f] */
    MORPH_BLACKHAT(opencv_imgproc.MORPH_BLACKHAT);

    public final int value;

    MorphTypesEnum(int value) {
        this.value = value;
    }
}
