cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

add_library( # Sets the name → must match System.loadLibrary("...")
        native-lib
        SHARED
        native-lib.cpp )

find_library( # Sets the name of the path variable.
        log-lib
        log )

target_link_libraries( # Specifies the target library.
        native-lib
        ${log-lib} )
