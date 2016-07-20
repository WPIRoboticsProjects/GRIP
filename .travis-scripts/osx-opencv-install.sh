#!/bin/bash

brew update
brew install gcc
brew upgrade gcc
brew install cmake
brew upgrade cmake
brew install python3
brew upgrade python3
pip3 install numpy
pip3 install numpy --ugrade
git clone https://github.com/opencv/opencv.git
cd opencv
mkdir build
cd build
cmake ..
make
sudo make install
export JNIDIR=/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers/
export OPEN_CV_LIB=/usr/local/lib
export OPEN_CV_JAR=/usr/local/share/OpenCV/java/opencv-310.jar
cd ../..

