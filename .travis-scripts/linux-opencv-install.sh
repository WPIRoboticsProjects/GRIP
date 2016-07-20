pip3 install numpy
git clone https://github.com/Itseez/opencv.git
cd opencv
mkdir build
cd build
cmake -D CMAKE_BUILD_TYPE=RELEASE \
-D INSTALL_C_EXAMPLES=OFF \ 
-D BUILD_EXAMPLES=ON ..
make
make install
