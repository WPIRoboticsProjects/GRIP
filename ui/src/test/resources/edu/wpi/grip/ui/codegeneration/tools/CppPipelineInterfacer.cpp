#include "edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer.h"
#include "Handle.h"
#include "AbsPipeline.h"
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <String>
#include <dlfcn.h>
#include <vector>
typedef AbsPipeline* maker();

string jstringToString(JNIEnv *env, jstring jstr){
  const char* cstr = env->GetStringUTFChars(jstr, 0);
  string str = string(cstr);
  env->ReleaseStringUTFChars(jstr, cstr);
  return str;

}

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

JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getMatNative
  (JNIEnv *env, jobject obj, jstring outName, jlong handle){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      string name = jstringToString(env, outName);
      Mat * out = (Mat *) (inst->*(inst->getOutputs()[name]))();
      Mat * dest = reinterpret_cast<Mat *>(handle); 
      out->copyTo(*dest);
  }
  
  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setMatSource
  (JNIEnv *env, jobject obj, jint sourceNum, jstring fileName){
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    string name = jstringToString(env, fileName);
    Mat img = imread(name, 1);
    (inst->*(inst->getMatSources()[(int) sourceNum]))(&img);
  }
  
  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setCondition
  (JNIEnv *env, jobject obj, jstring funName, jboolean value){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      bool val = (bool) value;
      string name = jstringToString(env, funName);
      (inst->*(inst->getConditions()[name]))(val);
  }
  
  JNIEXPORT jboolean JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBoolean
  (JNIEnv *env, jobject obj, jstring funName){
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    string name = jstringToString(env, funName);
    bool * output = (bool *) (inst->*(inst->getOutputs()[name]))();
    return (jboolean) *output;
  }
  
  JNIEXPORT jdouble JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getDouble
  (JNIEnv *env, jobject obj, jstring funName){
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    string name = jstringToString(env, funName);
    double * output = (double *) (inst->*(inst->getOutputs()[name]))();
    return (jdouble) *output;
  }
  
  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_dispose
  (JNIEnv *env , jobject obj){
      AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
      void * libHandle = inst->libHandle;
      delete inst;
      dlclose(libHandle);
  }

  
  JNIEXPORT jdoubleArray JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getSizeOrPoint
  (JNIEnv *env, jobject obj, jstring funName, jboolean isSize){
  	int numEles = 2;
    double vals[numEles];
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    string name = jstringToString(env, funName);
    if(isSize){
      Size * sz = (Size *)(inst->*(inst->getOutputs()[name]))();
      vals[0] = sz->width;
      vals[1] = sz->height;
    }
    else{
      Point * pnt = (Point *)(inst->*(inst->getOutputs()[name]))();
      vals[0] = pnt->x;
      vals[1] = pnt->y;
    }
    jdoubleArray retval = env->NewDoubleArray(numEles);
    env->SetDoubleArrayRegion(retval, 0, numEles, vals);
    return retval;
  }

  void KeyPointVectorToMat(vector<KeyPoint>& v_kp, Mat& mat)
{
    int count = (int)v_kp.size();
    mat.create(count, 1, CV_32FC(7));
    for(int i=0; i<count; i++)
    {
        KeyPoint kp = v_kp[i];
        mat.at< Vec<float, 7> >(i, 0) = Vec<float, 7>(kp.pt.x, kp.pt.y, kp.size, kp.angle, kp.response, (float)kp.octave, (float)kp.class_id);
    }
}

  JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getBlobs
  (JNIEnv *env, jobject obj, jstring funName, jlong outAdr){
    AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
    string name = jstringToString(env, funName);
    vector<KeyPoint> * output = (vector<KeyPoint> *) (inst->*(inst->getOutputs()[name]))();
    Mat* out = (Mat*) outAdr;
    KeyPointVectorToMat(*output, *out);    
  }

JNIEXPORT jint JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getNumContours
  (JNIEnv *env, jobject obj, jstring funName){
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  vector<vector<Point> > * output = (vector<vector<Point> > *) (inst->*(inst->getOutputs()[name]))();
  return (jint) output->size();
}

JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getContours
  (JNIEnv *env, jobject obj, jstring funName, jlongArray addresses){
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  string name = jstringToString(env, funName);
  vector<vector<Point> > * output = (vector<vector<Point> > *) (inst->*(inst->getOutputs()[name]))();
  jsize len = env->GetArrayLength(addresses);
  jlong *addrs = env->GetLongArrayElements(addresses, 0);
  for(int idx = 0; idx < len; idx++){
    Mat temp = Mat((*output)[idx], true);
    temp.copyTo(*(Mat *) addrs[idx]);
  }
  env->ReleaseLongArrayElements(addresses, addrs, 0);
}

typedef vector<Vec6d> LineFun(string, AbsPipeline*);
JNIEXPORT jobjectArray JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_getLines
  (JNIEnv *env, jobject obj, jstring funName){
  AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
  LineFun* getLines =(LineFun *) dlsym(inst->libHandle, "getLines");
  string name = jstringToString(env, funName);
  vector<Vec6d> lines = getLines(name, inst);
  jclass dblArray = env->FindClass("[D");
  jint numLines = lines.size();
  jint numEles = 6;
  jobjectArray linesArray = env->NewObjectArray(numLines, dblArray, NULL);
  for(jint idx = 0; idx< numLines; idx++){
  	jdoubleArray data = env->NewDoubleArray(numEles);
  	Vec6d line = lines[idx];
  	jdouble dblData[numEles];
  	for(int ele = 0; ele<numEles; ele++){
  		dblData[ele] = line[ele];
  	}
  	env->SetDoubleArrayRegion(data, 0, numEles, dblData);
  	env->SetObjectArrayElement(linesArray, idx, data);
  }
  return linesArray;
}

JNIEXPORT void JNICALL Java_edu_wpi_grip_ui_codegeneration_tools_CppPipelineInterfacer_setNumSource
  (JNIEnv *env, jobject obj, jint num, jdouble value){
   AbsPipeline *inst = getHandle<AbsPipeline>(env, obj);
   double* input = (double *) malloc(sizeof(double));
   *input = (double) value;
   (inst->*(inst->getNumSources()[(int) num]))(input);
  }
