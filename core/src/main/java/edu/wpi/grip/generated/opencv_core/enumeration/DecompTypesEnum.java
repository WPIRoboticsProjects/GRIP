package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum DecompTypesEnum {

  /**
   * Gaussian elimination with the optimal pivot element chosen.
   */
  DECOMP_LU(opencv_core.DECOMP_LU),
  /**
   * singular value decomposition (SVD) method; the system can be over-defined and/or the matrix
   * src1 can be singular
   */
  DECOMP_SVD(opencv_core.DECOMP_SVD),
  /**
   * eigenvalue decomposition; the matrix src1 must be symmetrical
   */
  DECOMP_EIG(opencv_core.DECOMP_EIG),
  /**
   * Cholesky \f$LL^T\f$ factorization; the matrix src1 must be symmetrical and positively defined
   */
  DECOMP_CHOLESKY(opencv_core.DECOMP_CHOLESKY),
  /**
   * QR factorization; the system can be over-defined and/or the matrix src1 can be singular
   */
  DECOMP_QR(opencv_core.DECOMP_QR),
  /**
   * while all the previous flags are mutually exclusive, this flag can be used together with any of
   * the previous; it means that the normal equations \f$\texttt{src1}^T\cdot\texttt{src1}\cdot\texttt{dst}=\texttt{src1}^T\texttt{src2}\f$
   * are solved instead of the original system \f$\texttt{src1}\cdot\texttt{dst}=\texttt{src2}\f$
   */
  DECOMP_NORMAL(opencv_core.DECOMP_NORMAL);

  public final int value;

  DecompTypesEnum(int value) {
    this.value = value;
  }
}
