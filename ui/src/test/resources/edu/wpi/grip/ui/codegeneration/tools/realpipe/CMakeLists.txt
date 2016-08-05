cmake_minimum_required(VERSION 3.5.2)
set(LINK_OPTIONS -O2)
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L. -L$ENV{OPEN_CV_LIB}")
set(CV_HEADERS $ENV{OPEN_CV_HEADERS})
if(${CV_HEADERS})
    include_directories(${OPEN_CV_HEADERS})
endif(${CV_HEADERS})
set(NAME "Pipeline" CACHE STRING "The file to compile")
include_directories(${CMAKE_SOURCE_DIR}/pipelib/pipe/)
LINK_DIRECTORIES(${CMAKE_SOURCE_DIR}/pipelib/pipe)
IF(${CMAKE_SYSTEM_NAME MATCHES "Windows"})
    add_definitions(-DON_WIN)
ENDIF(${CMAKE_SYSTEM_NAME MATCHES "Windows"})
set ( CXX_SRCS
    ${CMAKE_SOURCE_DIR}/${NAME}
    )
add_library(${NAME} SHARED ${CXX_SRCS})
target_link_libraries(${NAME} ${LINK_OPTIONS})
target_link_libraries(${NAME} 
    opencv_core.3.1.0
    opencv_objdetect.3.1.0
    opencv_features2d.3.1.0
    opencv_imgcodecs.3.1.0
    opencv_imgproc.3.1.0
    opencv_highgui.3.1.0
    pipe
)
