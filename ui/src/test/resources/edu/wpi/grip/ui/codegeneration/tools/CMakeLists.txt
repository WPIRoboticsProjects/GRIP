cmake_minimum_required(VERSION 3.5.2)
MACRO(SUBDIRLIST result curdir)
  FILE(GLOB children ${curdir}/*)
  SET(dirlist "")
  FOREACH(child ${children})
    IF(IS_DIRECTORY ${child})
      LIST(APPEND dirlist ${child})
    ENDIF()
  ENDFOREACH()
  SET(${result} ${dirlist})
ENDMACRO()
MACRO(normalizeEnv result var)
string(REPLACE "\\" "/" ${result} $ENV{${var}})
message("${${result}}")
ENDMACRO()
add_subdirectory(pipe)
normalizeEnv(JNIDIR "JNIDIR")
set(LINK_OPTIONS -O2)
include_directories(${JNIDIR})
include_directories(pipe/)
include_directories(${CV_HEADER_LOCATION})
set ( CXX_SRCS
    ${CMAKE_SOURCE_DIR}/CppPipelineInterfacer.cpp
    )
link_directories(${CMAKE_SOURCE_DIR}/pipe/)
add_library(genJNI SHARED ${CXX_SRCS} ${CV_HEADER_LOCATION})
target_include_directories(genJNI PUBLIC ${CV_HEADER_LOCATION})
if(NOT ${CMAKE_SYSTEM_NAME} MATCHES "Windows")
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
endif(NOT ${CMAKE_SYSTEM_NAME} MATCHES "Windows")
if(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
    add_definitions(-DON_WIN)
    set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L. -L$ENV{OPEN_CV_LIB}")
    normalizeEnv(CV_HEADER_LOCATION "OPEN_CV_HEADERS")
    normalizeEnv(CV_EXPORT "OPEN_CV_EXPORTS")
    SUBDIRLIST(JNI_SUB_DIRS ${JNIDIR})
    include_directories(${JNI_SUB_DIRS})
    link_directories(${CV_EXPORT})
    string(REPLACE "//" "/" CV_REGEX "${CV_EXPORT}/*.lib")
    file(GLOB CV_WIN_LIBS false ${CV_REGEX})
    foreach(CV_LIB ${CV_WIN_LIBS})
        target_link_libraries(genJNI ${CV_LIB})
    endforeach(CV_LIB ${CV_WIN_LIBS})
    target_link_libraries(genJNI
        pipe
        )
endif(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
message("JNI CMAKE done!")
