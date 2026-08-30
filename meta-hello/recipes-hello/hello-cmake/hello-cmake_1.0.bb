SUMMARY = "Hello world built with CMake"
LICENSE = "CLOSED"

SRC_URI = "file://CMakeLists.txt \
           file://main.c"

S = "${WORKDIR}"

inherit cmake
