package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum NormTypesEnum {

    /**
                \f[
                norm =  \forkthree
                {\|\texttt{src1}\|_{L_{\infty}} =  \max _I | \texttt{src1} (I)|}{if  \(\texttt{normType} = \texttt{NORM_INF}\) }
                {\|\texttt{src1}-\texttt{src2}\|_{L_{\infty}} =  \max _I | \texttt{src1} (I) -  \texttt{src2} (I)|}{if  \(\texttt{normType} = \texttt{NORM_INF}\) }
                {\frac{\|\texttt{src1}-\texttt{src2}\|_{L_{\infty}}    }{\|\texttt{src2}\|_{L_{\infty}} }}{if  \(\texttt{normType} = \texttt{NORM_RELATIVE | NORM_INF}\) }
                \f]
                */
    NORM_INF(opencv_core.NORM_INF), /**
                \f[
                norm =  \forkthree
                {\| \texttt{src1} \| _{L_1} =  \sum _I | \texttt{src1} (I)|}{if  \(\texttt{normType} = \texttt{NORM_L1}\)}
                { \| \texttt{src1} - \texttt{src2} \| _{L_1} =  \sum _I | \texttt{src1} (I) -  \texttt{src2} (I)|}{if  \(\texttt{normType} = \texttt{NORM_L1}\) }
                { \frac{\|\texttt{src1}-\texttt{src2}\|_{L_1} }{\|\texttt{src2}\|_{L_1}} }{if  \(\texttt{normType} = \texttt{NORM_RELATIVE | NORM_L1}\) }
                \f]*/
    NORM_L1(opencv_core.NORM_L1), /**
                 \f[
                 norm =  \forkthree
                 { \| \texttt{src1} \| _{L_2} =  \sqrt{\sum_I \texttt{src1}(I)^2} }{if  \(\texttt{normType} = \texttt{NORM_L2}\) }
                 { \| \texttt{src1} - \texttt{src2} \| _{L_2} =  \sqrt{\sum_I (\texttt{src1}(I) - \texttt{src2}(I))^2} }{if  \(\texttt{normType} = \texttt{NORM_L2}\) }
                 { \frac{\|\texttt{src1}-\texttt{src2}\|_{L_2} }{\|\texttt{src2}\|_{L_2}} }{if  \(\texttt{normType} = \texttt{NORM_RELATIVE | NORM_L2}\) }
                 \f]
                 */
    NORM_L2(opencv_core.NORM_L2), /**
                 \f[
                 norm =  \forkthree
                 { \| \texttt{src1} \| _{L_2} ^{2} = \sum_I \texttt{src1}(I)^2} {if  \(\texttt{normType} = \texttt{NORM_L2SQR}\)}
                 { \| \texttt{src1} - \texttt{src2} \| _{L_2} ^{2} =  \sum_I (\texttt{src1}(I) - \texttt{src2}(I))^2 }{if  \(\texttt{normType} = \texttt{NORM_L2SQR}\) }
                 { \left(\frac{\|\texttt{src1}-\texttt{src2}\|_{L_2} }{\|\texttt{src2}\|_{L_2}}\right)^2 }{if  \(\texttt{normType} = \texttt{NORM_RELATIVE | NORM_L2}\) }
                 \f]
                 */
    NORM_L2SQR(opencv_core.NORM_L2SQR), /**
                 In the case of one input array, calculates the Hamming distance of the array from zero,
                 In the case of two input arrays, calculates the Hamming distance between the arrays.
                 */
    NORM_HAMMING(opencv_core.NORM_HAMMING), /**
                 Similar to NORM_HAMMING, but in the calculation, each two bits of the input sequence will
                 be added and treated as a single bit to be used in the same calculation as NORM_HAMMING.
                 */
    NORM_HAMMING2(opencv_core.NORM_HAMMING2), /** bit-mask which can be used to separate norm type from norm flags */
    NORM_TYPE_MASK(opencv_core.NORM_TYPE_MASK), /** flag */
    NORM_RELATIVE(opencv_core.NORM_RELATIVE), /** flag */
    NORM_MINMAX(opencv_core.NORM_MINMAX);

    public final int value;

    NormTypesEnum(int value) {
        this.value = value;
    }
}
