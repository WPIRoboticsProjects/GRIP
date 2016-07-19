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
using namespace cv;
using namespace std;

class AbsPipeline{
    
public:
    typedef void (AbsPipeline::*MatSource)(Mat&);
    typedef void* (AbsPipeline::*Output)();
    typedef void (AbsPipeline::*Condition)(bool);
    typedef void (AbsPipeline::*NumSource)(double);
    virtual void Process() = 0;
    map<int, MatSource> getMatSources();
    map<String, Output> getOutputs();
    map<String, Condition> getConditions();
    map<int, NumSource> getNumSources();
    void* libHandle;
    virtual ~AbsPipeline();
protected:
    map<int, MatSource> matSources;
    map<String, Output> outputs;
    map<String, Condition> conditions;
    map<int, NumSource> numSources;
};
