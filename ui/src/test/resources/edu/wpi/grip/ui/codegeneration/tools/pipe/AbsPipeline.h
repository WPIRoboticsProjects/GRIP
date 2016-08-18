#pragma once
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d.hpp>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <string>

#ifdef ON_WIN
#ifdef WIN_EXPORT
//On Windows and creating the DLL of the real Pipeline
#define DLL_TYPE __declspec(dllexport)
#else
//On Windows and using the header to link against the Pipeline
#define DLL_TYPE __declspec(dllimport)
#endif
#else
//Not on Windows so don't have to do any of this __declspec nonsense.
//Still have to define the constant so that DLL_Type disappears in the preprocessed code.
#define DLL_TYPE
#endif

class DLL_TYPE AbsPipeline{
    
public:
    typedef void (AbsPipeline::*MatSource)(cv::Mat&);
    typedef void* (AbsPipeline::*Output)();
    typedef void (AbsPipeline::*Condition)(bool);
    typedef void (AbsPipeline::*NumSource)(double);
    virtual void Process() = 0;
    std::map<int, MatSource> getMatSources();
    std::map<std::string, Output> getOutputs();
    std::map<std::string, Condition> getConditions();
    std::map<int, NumSource> getNumSources();
    void* libHandle;
    virtual ~AbsPipeline();
protected:
    std::map<int, MatSource> matSources;
    std::map<std::string, Output> outputs;
    std::map<std::string, Condition> conditions;
    std::map<int, NumSource> numSources;
};
