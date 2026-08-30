SUMMARY = "Hello world built with CMake"
LICENSE = "CLOSED"

# For local files - not generally recommended?
SRC_URI = "file://CMakeLists.txt \
           file://main.c"
S = "${WORKDIR}"

inherit cmake
