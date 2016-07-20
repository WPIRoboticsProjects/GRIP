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
vers=$(python3 -c 'import platform; print(platform.python_version())')
git clone https://github.com/Itseez/opencv.git
cd opencv
mkdir build
cd build
cmake -D CMAKE_BUILD_TYPE=RELEASE \
 -D PYTHON3_EXECUTABLE=$(which python3) \
 -D PYTHON3_INCLUDE_DIR=$(python3 -c "from distutils.sysconfig import get_python_inc; print(get_python_inc())") \
 -D PYTHON3_LIBRARY=/usr/local/Cellar/python3/$vers/Frameworks/Python.framework/Versions/3.5/lib/libpython3.5.dylib \
 -D PYTHON3_LIBRARIES=/usr/local/Cellar/python3/$vers/Frameworks/Python.framework/Versions/3.5/bin \
 -D PYTHON3_INCLUDE_DIR=/usr/local/Cellar/python3/3.5.1/Frameworks/Python.framework/Versions/3.5/Headers \
 -D PYTHON3_PACKAGES_PATH=$(python3 -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())") \
 -D INSTALL_C_EXAMPLES=OFF -D INSTALL_PYTHON_EXAMPLES=ON \
 -D BUILD_EXAMPLES=ON ..
make
export JNIDIR=/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers/
export OPEN_CV_LIB=/usr/local/lib
export OPEN_CV_JAR=/usr/local/share/OpenCV/java/opencv-310.jar
cd ../..

