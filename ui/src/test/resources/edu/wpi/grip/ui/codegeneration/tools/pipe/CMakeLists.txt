cmake_minimum_required(VERSION 3.5.2)
project (AbsPipe C CXX)
set(LINK_OPTIONS -O2)
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L. -L/usr/local/lib ")
set ( CXX_SRCS
    AbsPipeline.cpp
    )
add_library(pipe SHARED ${CXX_SRCS})
target_link_libraries(pipe ${LINK_OPTIONS})
target_link_libraries(pipe 
    opencv_core.3.1.0
    opencv_objdetect.3.1.0
    opencv_features2d.3.1.0
    opencv_imgcodecs.3.1.0
    opencv_imgproc.3.1.0
    opencv_highgui.3.1.0
)
