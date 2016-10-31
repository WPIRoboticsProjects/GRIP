cmake_minimum_required(VERSION 3.5.2)
project (AbsPipe C CXX)
set(LINK_OPTIONS -O2)
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L. -L$ENV{OPEN_CV_LIB}")
set ( CXX_SRCS
    AbsPipeline.cpp
    )
add_library(pipe SHARED ${CXX_SRCS})
if(NOT ${CMAKE_SYSTEM_NAME} MATCHES "Windows")
    target_link_libraries(pipe ${LINK_OPTIONS})
    target_link_libraries(pipe 
        opencv_core.3.1.0
        opencv_objdetect.3.1.0
        opencv_features2d.3.1.0
        opencv_imgcodecs.3.1.0
        opencv_imgproc.3.1.0
        opencv_highgui.3.1.0
    )
endif(NOT ${CMAKE_SYSTEM_NAME} MATCHES "Windows")
if(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
add_definitions(-DON_WIN)
add_definitions(-DWIN_EXPORT)
string(REPLACE "\\" "/" CV_HEADERS $ENV{OPEN_CV_HEADERS})
include_directories(${CV_HEADERS})
    string(REPLACE "\\" "/" CV_EXPORT $ENV{OPEN_CV_EXPORTS})
    link_directories(${CV_EXPORT})
    file(GLOB CV_WIN_LIBS false ${CV_EXPORT}/*.lib)
    target_link_libraries(pipe
        ${CV_WIN_LIBS}
        )
endif(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
