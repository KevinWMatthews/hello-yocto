cmake_minimum_required(VERSION 3.28)
project(hello_cmake C)
add_executable(hello-cmake main.c)
install(TARGETS hello-cmake RUNTIME DESTINATION bin)
