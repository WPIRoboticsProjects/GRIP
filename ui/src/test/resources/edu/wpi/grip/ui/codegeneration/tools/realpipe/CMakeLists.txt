cmake_minimum_required(VERSION 3.5.2)
MACRO(normalizeEnv result var)
string(REPLACE "\\" "/" ${result} $ENV{${var}})
message("${${result}}")
ENDMACRO()
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L. -L$ENV{OPEN_CV_LIB}")
set(NAME "Pipeline" CACHE STRING "The file to compile")
include_directories(${CMAKE_SOURCE_DIR}/pipelib/pipe/)
LINK_DIRECTORIES(${CMAKE_SOURCE_DIR}/pipelib/pipe)
set ( CXX_SRCS
    ${CMAKE_SOURCE_DIR}/${NAME}
    )
add_library(${NAME} SHARED ${CXX_SRCS} ${CV_HEADERS})
target_include_directories(${NAME} PUBLIC ${CV_HEADERS})
IF(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
    add_definitions(-DON_WIN)
    add_definitions(-DWIN_EXPORT)
    normalizeEnv(CV_HEADERS "OPEN_CV_HEADERS")
    include_directories(${CV_HEADERS})
    normalizeEnv(CV_EXPORT "OPEN_CV_EXPORTS")
    link_directories(${CV_EXPORT})
    string(REPLACE "//" "/" CV_REGEX "${CV_EXPORT}/*.lib")
    file(GLOB CV_WIN_LIBS false ${CV_REGEX})
    foreach(CV_LIB ${CV_WIN_LIBS})
        target_link_libraries(${NAME} ${CV_LIB})
    endforeach(CV_LIB ${CV_WIN_LIBS})
    target_link_libraries(${NAME}
        pipe
        )
ELSE(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
    set(LINK_OPTIONS -O2)
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
ENDIF(${CMAKE_SYSTEM_NAME} MATCHES "Windows")

