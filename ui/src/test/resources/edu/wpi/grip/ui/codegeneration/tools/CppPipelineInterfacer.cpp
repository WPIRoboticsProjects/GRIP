#include "edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer.h"
#include "Handle.h"
#include "AbsPipeline.h"
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <String>
#include <dlfcn.h>
typedef AbsPipeline* maker();
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_init
  (JNIEnv *env , jobject obj, jstring libName){
      const char *lib= env->GetStringUTFChars(libName,0);
      void* libHandle = dlopen(lib, RTLD_NOW);
      maker* make = (maker*) dlsym(libHandle, "makePipeline");
      AbsPipeline *inst = make();
      inst->libHandle = libHandle;
      setHandle(env, obj, inst);
      env->ReleaseStringUTFChars(libName, lib);
  }
  
JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_process
  (JNIEnv *env , jobject obj){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      inst->Process();
  }

JNIEXPORT jstring JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getMatFile
  (JNIEnv *env, jobject obj, jint outNum){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      Mat * output = (Mat *) (inst->*(inst->getOutputs()[(int) outNum]))();
      std::string fileName = "mat" + std::to_string((int)outNum)+".png";
      imwrite(fileName, *output);
      return env->NewStringUTF(fileName.c_str());
  }
  
  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setMatSource
  (JNIEnv *env, jobject obj, jint sourceNum, jstring fileName){
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    const char *imgName= env->GetStringUTFChars(fileName,0);
    std::string name = string(imgName);
    Mat img = imread(imgName, 1);
    env->ReleaseStringUTFChars(fileName, imgName);
    (inst->*(inst->getMatSources()[(int) sourceNum]))(&img);
  }
  
  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setCondition
  (JNIEnv *env, jobject obj, jint boolNum, jboolean value){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      bool val = (bool) value;
      (inst->*(inst->getConditions()[(int) boolNum]))(val);
  }
  
  JNIEXPORT jboolean JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBoolean
  (JNIEnv *env, jobject obj, jint num){
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    bool * output = (bool *) (inst->*(inst->getOutputs()[(int) num]))();
    return (jboolean) *output;
  }
  
  JNIEXPORT jdouble JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getDouble
  (JNIEnv *env, jobject obj, jint num){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    double * output = (double *) (inst->*(inst->getOutputs()[(int) num]))();
    return (jdouble) *output;
  }
  
  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_dispose
  (JNIEnv *env , jobject obj){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      delete inst;
  }