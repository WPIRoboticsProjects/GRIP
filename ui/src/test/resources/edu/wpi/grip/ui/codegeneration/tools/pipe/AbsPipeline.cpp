#include "AbsPipeline.h"
map<int, AbsPipeline::MatSource> AbsPipeline::getMatSources(){
    return this->matSources;
}
map<String, AbsPipeline::Output> AbsPipeline::getOutputs(){
    return this->outputs;
}
map<String, AbsPipeline::Condition> AbsPipeline::getConditions(){
    return this->conditions;
}
AbsPipeline::~AbsPipeline(){}

map<int, AbsPipeline::NumSource> AbsPipeline::getNumSources(){
	return this->numSources;
}