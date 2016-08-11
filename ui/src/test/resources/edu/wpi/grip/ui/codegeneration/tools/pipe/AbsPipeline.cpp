#include "AbsPipeline.h"
using namespace std;
map<int, AbsPipeline::MatSource> AbsPipeline::getMatSources(){
    return this->matSources;
}
map<string, AbsPipeline::Output> AbsPipeline::getOutputs(){
    return this->outputs;
}
map<string, AbsPipeline::Condition> AbsPipeline::getConditions(){
    return this->conditions;
}
AbsPipeline::~AbsPipeline(){}

map<int, AbsPipeline::NumSource> AbsPipeline::getNumSources(){
	return this->numSources;
}
