#!/bin/bash

brew update
brew outdated gcc || brew upgrade gcc || brew install gcc
brew outdated cmake || brew upgrade cmake || brew install cmake
brew outdated python3 || brew upgrade python3 || brew install python3
brew uninstall numpy || brew install numpy --with-python3
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

