cmake_minimum_required(VERSION 3.28)
project(hello_in_tree C)
add_executable(hello-in-tree main.c)
install(TARGETS hello-in-tree RUNTIME DESTINATION bin)
