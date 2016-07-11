#include "AbsPipeline.h"
map<int, AbsPipeline::MatSource> AbsPipeline::getMatSources(){
    return this->matSources;
}
map<int, AbsPipeline::Output> AbsPipeline::getOutputs(){
    return this->outputs;
}
map<int, AbsPipeline::Condition> AbsPipeline::getConditions(){
    return this->conditions;
}
AbsPipeline::~AbsPipeline(){}

map<int, AbsPipeline::NumSource> AbsPipeline::getNumSources(){
	return this->numSources;
}