#!/bin/bash

brew update
brew install gcc
brew upgrade gcc
brew install cmake
brew upgrade cmake
brew install ant
brew upgrade ant
brew install python3
brew linkapps python3
pip3 install numpy
pip3 install opencv-python
mkdir -p $HOME/opencv/jni
