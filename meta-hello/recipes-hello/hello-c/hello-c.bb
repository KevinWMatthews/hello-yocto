# Build an external application, 'hello world'
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

# TODO Find the most idiomatic testing workflow
# NOTE: to build from a local git repo, use:
# SRC_URI = "git:///${TOPDIR}/../path/to/hello_c;protocol=file;branch=main"
# SRCREV = "${AUTOREV}"
# NOTE: this requires source to be in hard-coded relative directory!
# This isn't a recommended approach.
# NOTE: the source directory mounted in the container during build
# Patches the external source for good measure
SRC_URI = "git://github.com/KevinWMatthews/hello-c.git;protocol=https;branch=main \
           file://0001-create-changes-in-Yocto-build-environment.patch \
           file://0001-modify-the-project-from-the-devtool-workspace.patch \
           "
SRCREV = "26c2399b71c0c17eec77052758b188c58462fe28"
S = "${WORKDIR}/git"

inherit cmake

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE = ""

