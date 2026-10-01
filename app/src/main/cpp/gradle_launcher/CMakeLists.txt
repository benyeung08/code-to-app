
cmake_minimum_required(VERSION 3.18.1)
project(gradle_launcher)

add_library(gradle_launcher SHARED
        gradle_launcher.cpp
)

find_library(log-lib log)

target_link_libraries(gradle_launcher
        ${log-lib}
)
