package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum ErrorCodeEnum {

  /**
   * everithing is ok
   */
  StsOk(opencv_core.StsOk),
  /**
   * pseudo error for back trace
   */
  StsBackTrace(opencv_core.StsBackTrace),
  /**
   * unknown /unspecified error
   */
  StsError(opencv_core.StsError),
  /**
   * internal error (bad state)
   */
  StsInternal(opencv_core.StsInternal),
  /**
   * insufficient memory
   */
  StsNoMem(opencv_core.StsNoMem),
  /**
   * function arg/param is bad
   */
  StsBadArg(opencv_core.StsBadArg),
  /**
   * unsupported function
   */
  StsBadFunc(opencv_core.StsBadFunc),
  /**
   * iter. didn't converge
   */
  StsNoConv(opencv_core.StsNoConv),
  /**
   * tracing
   */
  StsAutoTrace(opencv_core.StsAutoTrace),
  /**
   * image header is NULL
   */
  HeaderIsNull(opencv_core.HeaderIsNull),
  /**
   * image size is invalid
   */
  BadImageSize(opencv_core.BadImageSize),
  /**
   * offset is invalid
   */
  BadOffset(opencv_core.BadOffset), BadDataPtr(opencv_core.BadDataPtr), BadStep(opencv_core.BadStep), BadModelOrChSeq(opencv_core.BadModelOrChSeq), BadNumChannels(opencv_core.BadNumChannels), BadNumChannel1U(opencv_core.BadNumChannel1U), BadDepth(opencv_core.BadDepth), BadAlphaChannel(opencv_core.BadAlphaChannel), BadOrder(opencv_core.BadOrder), BadOrigin(opencv_core.BadOrigin), BadAlign(opencv_core.BadAlign), BadCallBack(opencv_core.BadCallBack), BadTileSize(opencv_core.BadTileSize), BadCOI(opencv_core.BadCOI), BadROISize(opencv_core.BadROISize), MaskIsTiled(opencv_core.MaskIsTiled),
  /**
   * null pointer
   */
  StsNullPtr(opencv_core.StsNullPtr),
  /**
   * incorrect vector length
   */
  StsVecLengthErr(opencv_core.StsVecLengthErr),
  /**
   * incorr. filter structure content
   */
  StsFilterStructContentErr(opencv_core.StsFilterStructContentErr),
  /**
   * incorr. transform kernel content
   */
  StsKernelStructContentErr(opencv_core.StsKernelStructContentErr),
  /**
   * incorrect filter ofset value
   */
  StsFilterOffsetErr(opencv_core.StsFilterOffsetErr),
  /**
   * the input/output structure size is incorrect
   */
  StsBadSize(opencv_core.StsBadSize),
  /**
   * division by zero
   */
  StsDivByZero(opencv_core.StsDivByZero),
  /**
   * in-place operation is not supported
   */
  StsInplaceNotSupported(opencv_core.StsInplaceNotSupported),
  /**
   * request can't be completed
   */
  StsObjectNotFound(opencv_core.StsObjectNotFound),
  /**
   * formats of input/output arrays differ
   */
  StsUnmatchedFormats(opencv_core.StsUnmatchedFormats),
  /**
   * flag is wrong or not supported
   */
  StsBadFlag(opencv_core.StsBadFlag),
  /**
   * bad CvPoint
   */
  StsBadPoint(opencv_core.StsBadPoint),
  /**
   * bad format of mask (neither 8uC1 nor 8sC1)
   */
  StsBadMask(opencv_core.StsBadMask),
  /**
   * sizes of input/output structures do not match
   */
  StsUnmatchedSizes(opencv_core.StsUnmatchedSizes),
  /**
   * the data format/type is not supported by the function
   */
  StsUnsupportedFormat(opencv_core.StsUnsupportedFormat),
  /**
   * some of parameters are out of range
   */
  StsOutOfRange(opencv_core.StsOutOfRange),
  /**
   * invalid syntax/structure of the parsed file
   */
  StsParseError(opencv_core.StsParseError),
  /**
   * the requested function/feature is not implemented
   */
  StsNotImplemented(opencv_core.StsNotImplemented),
  /**
   * an allocated block has been corrupted
   */
  StsBadMemBlock(opencv_core.StsBadMemBlock),
  /**
   * assertion failed
   */
  StsAssert(opencv_core.StsAssert), GpuNotSupported(opencv_core.GpuNotSupported), GpuApiCallError(opencv_core.GpuApiCallError), OpenGlNotSupported(opencv_core.OpenGlNotSupported), OpenGlApiCallError(opencv_core.OpenGlApiCallError), OpenCLApiCallError(opencv_core.OpenCLApiCallError), OpenCLDoubleNotSupported(opencv_core.OpenCLDoubleNotSupported), OpenCLInitError(opencv_core.OpenCLInitError), OpenCLNoAMDBlasFft(opencv_core.OpenCLNoAMDBlasFft);

  public final int value;

  ErrorCodeEnum(int value) {
    this.value = value;
  }
}
