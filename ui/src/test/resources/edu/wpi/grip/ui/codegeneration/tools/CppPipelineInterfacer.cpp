#include "edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer.h"
#include "Handle.h"
#include "AbsPipeline.h"
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <string>
#ifndef ON_WIN
#include <dlfcn.h>
#else
#include <windows.h>
#endif
#include <vector>
using namespace std;
/**
* @file CppPipelineInterfacer.cpp
* @brief The implementation of the native methods for edu.wpi.grip.ui.codegeneration.tools.CppPipelineInterfacer java class
*
* Note that all the methods that take a jstring can crash the jvm if called with a jstring that does not match 
* any function in the given map.
*/

typedef AbsPipeline* maker();

/**
* Takes a Java string and the JNI enviroment and converts the jstring to a c++ string.
* @param env Pointer to the JNIEnv that the function is running inside of. 
* @param jstr the jstring to convert.
* @return a c++ string with the same characters as the java string.
*/
string jstringToString(JNIEnv *env, jstring jstr) {
  const char* cstr = env->GetStringUTFChars(jstr, 0);
  string str = string(cstr);
  env->ReleaseStringUTFChars(jstr, cstr);
  return str;
}

/**
* Initializes the pipeline object from the shared library given by the input string.
* This modifies the nativeHandle variable inside of the java object to point to the created instance of the pipeline.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param libName the full name of the library to be loaded. note this is language specific.
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_init
  (JNIEnv *env , jobject obj, jstring libName) {
  const char *lib= env->GetStringUTFChars(libName,0);
#ifndef ON_WIN
  void* libHandle = dlopen(lib, RTLD_NOW);
  maker* make = (maker*) dlsym(libHandle, "makePipeline");
#else
  void* libHandle = LoadLibrary(lib);
  maker* make = (maker*) GetProcAddress((HMODULE)libHandle, "makePipeline");
  #endif
  AbsPipeline *inst = make();
  inst->libHandle = libHandle;
  setHandle(env, obj, inst);
  env->ReleaseStringUTFChars(libName, lib);
}

/**
* Calls the process method on the associated pipeline object.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_process
  (JNIEnv *env , jobject obj) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  inst->Process();
}

/**
* Sets the mat pointed to by handle to be a copy of the mat returned by function indicated by outName.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param outName the name of the output function in the map.
* @param handle the nativeObj variable from the mat to set. 
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getMatNative
  (JNIEnv *env, jobject obj, jstring outName, jlong handle) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, outName);
  cv::Mat * out = (cv::Mat *) (inst->*(inst->getOutputs()[name]))();
  cv::Mat * dest = reinterpret_cast<cv::Mat *>(handle);
  out->copyTo(*dest);
}

/**
* Sets a source to a mat obtained from image at given file location.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param sourceNum the number of the source to set as the given image. 
* If this source is not correct (i.e. is not a valid image source number), 
* unexpected behaviour can occur (up to and including jvm crashing)
* @param fileName the location of the file that contains the image to use as a source.
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setMatSource
  (JNIEnv *env, jobject obj, jint sourceNum, jstring fileName) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, fileName);
  cv::Mat img = cv::imread(name, 1);
  (inst->*(inst->getMatSources()[(int) sourceNum]))(img);
}
  
/**
* Sets a boolean condition (for switch or valve) to given value.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the function to set the given condition for.
* @param value the boolean value to set the condition to.
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setCondition
  (JNIEnv *env, jobject obj, jstring funName, jboolean value) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  bool val = (bool) value;
  string name = jstringToString(env, funName);
  (inst->*(inst->getConditions()[name]))(val);
}

/**
* Gets a boolean output from given function name.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @return the output of the function.
*/
JNIEXPORT jboolean JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBoolean
  (JNIEnv *env, jobject obj, jstring funName) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  bool * output = (bool *) (inst->*(inst->getOutputs()[name]))();
  return (jboolean) *output;
}

/**
* Gets a double output from given function name.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @return the output of the function.
*/
JNIEXPORT jdouble JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getDouble
  (JNIEnv *env, jobject obj, jstring funName) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  double * output = (double *) (inst->*(inst->getOutputs()[name]))();
  return (jdouble) *output;
}
  
/**
* Closes the loaded library, deletes the object created and frees memory used by the native code.
* Dispose should be called after the object is done being used but before the java object is garbage collected .
* Failure to call dispose will lead to memory leaks. 
* Once dispose is called the object will no longer function properly and should not be used.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_dispose
  (JNIEnv *env , jobject obj) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  void * libHandle = inst->libHandle;
  delete inst;
#ifndef ON_WIN
  dlclose(libHandle);
#else
  FreeLibrary((HMODULE) libHandle);
#endif
}

/*
* Gets a size or point output depending on the isSize variable.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @param isSize. true if the function returns a size, false if it returns a point.
* @return an array of two doubles representing [width, height] for size or [x, y] for point.
*/
JNIEXPORT jdoubleArray JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getSizeOrPoint
  (JNIEnv *env, jobject obj, jstring funName, jboolean isSize) {
  const int numEles = 2;
  double vals[numEles];
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  if(isSize){
    cv::Size * sz = (cv::Size *)(inst->*(inst->getOutputs()[name]))();
    vals[0] = sz->width;
    vals[1] = sz->height;
  }
  else{
    cv::Point * pnt = (cv::Point *)(inst->*(inst->getOutputs()[name]))();
    vals[0] = pnt->x;
    vals[1] = pnt->y;
  }
  jdoubleArray retval = env->NewDoubleArray(numEles);
  env->SetDoubleArrayRegion(retval, 0, numEles, vals);
  return retval;
}

/**
* A helper method to convert a vector of KeyPoint to a mat.
* Copied from: http://answers.opencv.org/question/30869/how-to-pass-a-matofkeypoint-and-matofpoint2f-to-native-code-opencv-4-android/
* @param v_kp a vector of keypoints
* @param mat the mat to fill with the keypoints.
*/
void KeyPointVectorToMat(vector<cv::KeyPoint>& v_kp, cv::Mat& mat) {
  int count = (int)v_kp.size();
  mat.create(count, 1, CV_32FC(7));
  for(int i=0; i<count; i++) {
    cv::KeyPoint kp = v_kp[i];
    mat.at< cv::Vec<float, 7> >(i, 0) = cv::Vec<float, 7>(kp.pt.x, kp.pt.y, kp.size, kp.angle, kp.response, (float)kp.octave, (float)kp.class_id);
  }
}

/**
* Function to fill the given mat pointer with the vector of KeyPoint output from a blob function.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @param outAdr the native address of a mat to fill with data from the blob function.
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBlobs
  (JNIEnv *env, jobject obj, jstring funName, jlong outAdr) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  vector<cv::KeyPoint> * output = (vector<cv::KeyPoint> *) (inst->*(inst->getOutputs()[name]))();
  cv::Mat* out = (cv::Mat*) outAdr;
  KeyPointVectorToMat(*output, *out);    
}

/**
* Gets the number of contours contained in the given output function.
* This is used so the JVM can allocate an array to hold all of the contours
* for the getContours function.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @return the number of contours in the output.
*/
JNIEXPORT jint JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getNumContours
  (JNIEnv *env, jobject obj, jstring funName) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  vector<vector<cv::Point> > * output = (vector<vector<cv::Point> > *) (inst->*(inst->getOutputs()[name]))();
  return (jint) output->size();
}

/**
* Gets the contours from the given output.
* The addresses array should be an array of native addresses for Mats
* with length determined from getNumContours.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @param addresses an array of pointers to native addresses of mats to be used as outputs.
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getContours
  (JNIEnv *env, jobject obj, jstring funName, jlongArray addresses) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  vector<vector<cv::Point> > * output = (vector<vector<cv::Point> > *) (inst->*(inst->getOutputs()[name]))();
  jsize len = env->GetArrayLength(addresses);
  jlong *addrs = env->GetLongArrayElements(addresses, 0);
  for(int idx = 0; idx < len; idx++){
    cv::Mat temp = cv::Mat((*output)[idx], true);
    temp.copyTo(*(cv::Mat *) addrs[idx]);
  }
  env->ReleaseLongArrayElements(addresses, addrs, 0);
}

typedef vector<cv::Vec6d> LineFun(string, AbsPipeline*);

/**
* Returns an 2d array of doubles n by 6 containing n lines.
* each line is represented by [x1, y1, x2, y2, length, angle].
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param funName the name of the pipeline function to call.
* @return an array of doubles representing the lines. 
*/
JNIEXPORT jobjectArray JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getLines
  (JNIEnv *env, jobject obj, jstring funName) {
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
#ifndef ON_WIN
  LineFun* getLines =(LineFun *) dlsym(inst->libHandle, "getLines");
#else
  LineFun* getLines = (LineFun *) GetProcAddress((HMODULE)inst->libHandle, "getLines");
#endif
  string name = jstringToString(env, funName);
  vector<cv::Vec6d> lines = getLines(name, inst);
  jclass dblArray = env->FindClass("[D");
  jint numLines = lines.size();
  const jint numEles = 6;
  jobjectArray linesArray = env->NewObjectArray(numLines, dblArray, NULL);
  for(jint idx = 0; idx< numLines; idx++){
  	jdoubleArray data = env->NewDoubleArray(numEles);
  	cv::Vec6d line = lines[idx];
    jdouble dblData[numEles];
  	for(int ele = 0; ele<numEles; ele++){
  	  dblData[ele] = line[ele];
  	}
  	env->SetDoubleArrayRegion(data, 0, numEles, dblData);
  	env->SetObjectArrayElement(linesArray, idx, data);
  }
  return linesArray;
}

/**
* Sets a source to the given double value.
* If the source number is incorrect, unexpected behavior can occur. 
* Note this dynamically allocates the double and could potentially cause memory issues if called repeatedly.
* @param env Pointer to the JNIEnv that the function is running inside of.
* @param obj the object this function is part of. 
* @param num the source number to set.
* @param value the double value to set. 
*/
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setNumSource
  (JNIEnv *env, jobject obj, jint num, jdouble value) {
   AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
   (inst->*(inst->getNumSources()[(int) num]))((double)value);
  }
