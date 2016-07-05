cmake_minimum_required(VERSION 3.5.2)
add_subdirectory(pipe)
set(JNIDIR $ENV{JNIDIR})
set(LINK_OPTIONS -O2)
include_directories(${JNIDIR})
include_directories(pipe/)
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L. -L/usr/local/lib ")
set ( CXX_SRCS
    ${CMAKE_SOURCE_DIR}/CppPipelineInterfacer.cpp
    )
link_directories(${CMAKE_SOURCE_DIR}/pipe/)
add_library(genJNI SHARED ${CXX_SRCS})
target_link_libraries(genJNI ${LINK_OPTIONS})
target_link_libraries(genJNI 
    opencv_core.3.1.0
    opencv_objdetect.3.1.0
    opencv_features2d.3.1.0
    opencv_imgcodecs.3.1.0
    opencv_imgproc.3.1.0
    opencv_highgui.3.1.0
    pipe
)
