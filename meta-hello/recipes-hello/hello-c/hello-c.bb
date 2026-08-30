LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

# NOTE: this requires source to be in hard-coded relative directory!
# TODO the source is not mounted in the container
SRC_URI = "git:///${TOPDIR}/../src/hello_c;protocol=file;branch=main \
           file://0001-create-changes-in-Yocto-build-environment.patch \
           "
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/git"

inherit cmake

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE = ""

