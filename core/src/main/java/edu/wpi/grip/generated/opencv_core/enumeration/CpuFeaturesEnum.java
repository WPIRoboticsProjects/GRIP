package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum CpuFeaturesEnum {

  CPU_MMX(opencv_core.CPU_MMX), CPU_SSE(opencv_core.CPU_SSE), CPU_SSE2(opencv_core.CPU_SSE2), CPU_SSE3(opencv_core.CPU_SSE3), CPU_SSSE3(opencv_core.CPU_SSSE3), CPU_SSE4_1(opencv_core.CPU_SSE4_1), CPU_SSE4_2(opencv_core.CPU_SSE4_2), CPU_POPCNT(opencv_core.CPU_POPCNT), CPU_AVX(opencv_core.CPU_AVX), CPU_AVX2(opencv_core.CPU_AVX2), CPU_FMA3(opencv_core.CPU_FMA3), CPU_AVX_512F(opencv_core.CPU_AVX_512F), CPU_AVX_512BW(opencv_core.CPU_AVX_512BW), CPU_AVX_512CD(opencv_core.CPU_AVX_512CD), CPU_AVX_512DQ(opencv_core.CPU_AVX_512DQ), CPU_AVX_512ER(opencv_core.CPU_AVX_512ER), CPU_AVX_512IFMA512(opencv_core.CPU_AVX_512IFMA512), CPU_AVX_512PF(opencv_core.CPU_AVX_512PF), CPU_AVX_512VBMI(opencv_core.CPU_AVX_512VBMI), CPU_AVX_512VL(opencv_core.CPU_AVX_512VL), CPU_NEON(opencv_core.CPU_NEON);

  public final int value;

  CpuFeaturesEnum(int value) {
    this.value = value;
  }
}
