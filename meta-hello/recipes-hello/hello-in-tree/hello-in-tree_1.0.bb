# This builds from local files - not generally recommended but an easy way to start
SUMMARY = "Hello world built from source files in the Yocto tree"
LICENSE = "CLOSED"

SRC_URI = "file://CMakeLists.txt \
           file://main.c"
S = "${WORKDIR}"

inherit cmake
