cmake_minimum_required(VERSION 3.28)
project(hello C)
add_executable(hello main.c)
install(TARGETS hello RUNTIME DESTINATION bin)
