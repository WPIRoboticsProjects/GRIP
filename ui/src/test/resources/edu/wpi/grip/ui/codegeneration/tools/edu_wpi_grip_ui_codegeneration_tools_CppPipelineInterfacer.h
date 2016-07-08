/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer */

#ifndef _Included_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
#define _Included_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    process
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_process
  (JNIEnv *, jobject);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getMatFile
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getMatFile
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getDouble
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getDouble
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getBoolean
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBoolean
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    setCondition
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setCondition
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    setMatSource
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setMatSource
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    init
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_init
  (JNIEnv *, jobject, jstring);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_dispose
  (JNIEnv *, jobject);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getSizeOrPoint
 * Signature: (IZ)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getSizeOrPoint
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getBlobs
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBlobs
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getNumContours
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getNumContours
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getContours
 * Signature: (I[J)V
 */
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getContours
  (JNIEnv *, jobject, jint, jlongArray);

/*
 * Class:     edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer
 * Method:    getLines
 * Signature: (I)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getLines
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
