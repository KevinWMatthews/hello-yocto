# Development Guide

Notes on how to work with Yocto.

## Introduction

This project uses `kas-container` to run build commands in a podman (TODO docker?) container.

The container mounts the source code of this repository in the container directory `/work/`,
and the container starts in `/work/build/`.

To run individual commands in the container, use `shell -c`:

```bash
./kas-container shell kas/hello.yml -c "<command>"
```

Alternatively, shell into the container itself and run commands directly:

```bash
./kas-container shell kas/hello.yml 
/work/build $ <command>
```

For simplicity, the examples below are assumed to be run from within the container itself.
To run these commands from the host directly, use the `shell -c` form listed above.

## BitBake

TODO Add a brief explanation of what BitBake is.

Useful tools:

```bash
bitbake-layers --help
bitbake-layers show-layers
bitbake-layers show-recipes # there are many!
# Show all images (recipes in the image layer)
bitbake-layers show-recipes -i image
bitbake-layers show-appends

bitbake-layers create-layer
# Usually avoid tdhis, working with kas instead
bitbake-layers add-layer
```

## Build and Run with kas

```bash
./kas-container build kas/hello.yml
./kas-container shell kas/hello.yml -c "runqemu qemux86-64 nographic slirp"
```

Exit QEMU with:

```text
Ctrl-a, Ctrl-x
```

or by powering off the QEMU linux system:

```bash
root@qemux86-64# poweroff
```

## Build and Run with BitBake

For finer control of the build and development process, run the commands executed by `kas-container build` independently:

```bash
# Shell into build environment
./kas-container shell kas/hello.yml
# Build target (see hello.yml for the target)
/work/build/ $ bitbake -c build core-image-minimal
# Run image
/work/build/ $ runqemu qemux86-64 nographic slirp
```

## Package Development

Yocto typically clones remote source files and builds them, which makes a development workflow difficult.

TODO try this:
To test changes to an external repo that are committed and publicly available:

- perform development in an external repo
- commit and push changes
- modify `SRCREV` in the `.bb` file

TODO try this:
To actively develop an external repo locally, add it to the `devtool` workspace:

```bash
$ devtool modify <recipe>
INFO: Source tree extracted to /work/build/workspace/sources/<recipe>
INFO: Recipe <recipe> now set up to build from /work/build/workspace/sources/<recipe>
```

This copies source locally and informs `devtool` to build the recipe from a local copy (in the `build/workspace/sources/<recipe>` directory) instead of from the layer (`build/tmp/work/<layer>/<recipe>`).

TODO bitbake isn't aware of this?

This local source can be modified directly.

Build/rebuild the local source with:

```bash
devtool build <recipe>
```

To test changes, open a new container and run an existing image (TODO link to above kas/bitbake sections), then deploy the target:

```bash
devtool deploy-target <recipe> root@<target-ip>
```

runqemu - INFO - Network configuration: ip=dhcp
runqemu - INFO - Port forward: hostfwd=tcp:127.0.0.1:2222-:22 hostfwd=tcp:127.0.0.1:2323-:23

To build the entire image:

```bash
# See your top-level .yml file for the default target
devtool build-image core-image-minimal
```

This will **overwrite** any existing images that were built using bitbake!

Check which recipes are built from the workspace with:

```bash
devtool status
```

or by inspecting the workspace directly:

```bash
cd build
ls workspace/sources
```

Modifying a recipy with `devtool` creates a `.bbappend` file in `build/workspace/appends/<recipe>.bbappend`.
This overrides the layer's default configuration to point to local source files instead.

For more details, see `build/workspace/README`.

TODO try this again:
Another (not recommended) approach to develop an external repo is to copy files locally.

Note - these tools are provided by OpenEmbedded

- bitbake
- runqemu
- devtool

TODO `kas-container build` ignores the devtool workspace?

```text
bitbake -e core-image-minimal | grep ^EXTRA_IMAGE_FEATURES
cat conf/local.conf | grep dropbear

pipx run kas shell kas/hello.yml:kas/debug.yml -c "ssh -p 2222 root@127.0.0.1"
```

Can try running without slirp:

```text
KAS_CONTAINER_ENGINE=podman \
  ./kas-container --docker-args "--device /dev/net/tun --cap-add NET_ADMIN" shell kas/hello.yml:kas/debug.yml
```

Or by creating a pdoman/docker network and connecting two containers to it,
or by running a single container and opening a second podman into it.

cap-add failed. Try:

```text
runqemu qemux86-64 nographic slirp qemuparams="-netdev user,id=net0,hostfwd=tcp::2222-:22"

KAS_CONTAINER_ENGINE=podman
--docker-args is the same as --runtime-args
./kas-container --runtime-args "--name yocto-build" shell kas/hello.yml:kas/debug.yml

cd /work
. poky/oe-init-build-env build
devtool deploy-target <recipe> root@localhost -P 2222
```

QEMU has a console and help text:

```bash
cd /work
. poky/oe-init-build-env build
devtool deploy-target <recipe> root@localhost -P 2222
C-a h
C-a c
(qemu) help
```

```bash
builder@51f3d2d00d4b:/work/build$ runqemu qemux86-64 nographic slirp \
  qemuparams="-serial mon:unix:/tmp/ttyS0,server,nowait" \
  </dev/null >/tmp/qemu.log 2>&1 &
Expect to see:
builder@51f3d2d00d4b:/work/build$ jobs
[1]+ Running runqemu qemux86-64 nographic slirp qemuparams="-serial mon:unix:/tmp/ttyS0,server,nowait" < /dev/null > /tmp/qemu.log 2>&1 &

builder@51f3d2d00d4b:/work/build$ socat - UNIX-CONNECT:/tmp/ttyS0
```

Ctrl-o doesn't detach, but Ctrl-c does.

Wow, now this works:

```bash
# 1. Launch QEMU backgrounded, guest console on a socket:
runqemu qemux86-64 nographic slirp \
  qemuparams="-serial mon:unix:/tmp/ttyS0,server,nowait" \
  </dev/null >/tmp/qemu.log 2>&1 &

# 2. Attach to the guest console when needed (Ctrl-O detaches, QEMU keeps running):
socat -,raw,echo=0,escape=0x0f UNIX-CONNECT:/tmp/ttyS0

# 3. Deploy loop, from the container shell:
devtool build <recipe>
devtool deploy-target <recipe> root@localhost -P 2222
```

Power off the qemu when done and look for:

```text
[1]+  Done                    runqemu qemux86-64 nographic slirp qemuparams="-serial mon:unix:/tmp/ttyS0,server,nowait" < /dev/null > /tmp/qemu.log 2>&1
```

Refinements:

```bash
builder@61edf7ea92b9:/work/build$ QB_SERIAL_OPT="-serial mon:unix:/tmp/ttyS0,server,nowait" \
  runqemu qemux86-64 slirp qemuparams="-daemonize"
runqemu - INFO - Running MACHINE=qemux86-64 bitbake -e  ...
runqemu - INFO - Continuing with the following parameters:
KERNEL: [/work/build/tmp/deploy/images/qemux86-64/bzImage]
MACHINE: [qemux86-64]
FSTYPE: [ext4]
ROOTFS: [/work/build/tmp/deploy/images/qemux86-64/core-image-minimal-qemux86-64.rootfs-20260920155415.ext4]
CONFFILE: [/work/build/tmp/deploy/images/qemux86-64/core-image-minimal-qemux86-64.rootfs-20260920155415.qemuboot.conf]

runqemu - INFO - Network configuration: ip=dhcp
runqemu - INFO - Port forward: hostfwd=tcp:127.0.0.1:2222-:22 hostfwd=tcp:127.0.0.1:2323-:23
runqemu - INFO - Running /work/build/tmp/work/x86_64-linux/qemu-helper-native/1.0/recipe-sysroot-native/usr/bin/qemu-system-x86_64 -device virtio-net-pci,netdev=net0,mac=52:54:00:12:35:02 -netdev user,id=net0,hostfwd=tcp:127.0.0.1:2222-:22,hostfwd=tcp:127.0.0.1:2323'

runqemu - INFO - Host uptime: 359379.29

runqemu - INFO - Cleaning up
runqemu - INFO - Host uptime: 359379.34

builder@61edf7ea92b9:/work/build$ ps aux | grep qemu
builder      206  142  0.8 2047432 351004 ?      Sl   11:03   0:08 /work/build/tmp/work/x86_64-linux/qemu-helper-native/1.0/recipe-sysroot-native/usr/bin/qemu-system-x86_64 -device virtio-net-pci,netdev=net0,mac=52:54:00:12:35:02 -netdev user,id=net0,hostfwd=tcp:127
builder      217  0.0  0.0   6528  2240 pts/0    S+   11:03   0:00 grep qemu

or
builder@61edf7ea92b9:/work/build$ runqemu qemux86-64 slirp qemuparams="-serial mon:unix:/tmp/ttyS0,server,nowait -display none -daemonize"


Another idea:
./kas-container --runtime-args "--name yocto-build" shell kas/hello.yml:kas/debug.yml
```

Flow:

Modify config to add "ssh-server-dropbear" to `EXTRA_IMAGE_ARGS`.

```bash
# start kas-container
$ ./kas-container shell kas/hello.yml:kas/debug.yml

# start QEMU, daemonized
builder$ runqemu qemux86-64 slirp qemuparams="-serial mon:unix:/tmp/ttyS0,server,nowait -daemonize"

# Modify package
# Do this in your preferred editor on your host machine

# Rebuild package
builder$ devtool build hello-c

# Deploy rebuilt package
builder$ devtool deploy-target hello-c root@localhost -P 2222
INFO: Successfully deployed /work/build/tmp/work/core2-64-poky-linux/hello-c/1.0/image

# Connect to QEMU 
builder$ socat - UNIX-CONNECT:/tmp/ttyS0
# press Enter

# Inspect changes QEMU

# Disconnect from QEMU
Ctrl-d
Ctrl-c

# To keep changes:
# commit (and push?)
# then write changes back into the appropriate layer
builder$ devtool finish hello-c meta-hello
# This creates a patch with the changes
# Changes are also preserved locally, in build/workspace/attic/sources/<recipe>.<uid>

# Stop qemu
root@qemu$ poweroff
root@qemu$ Ctrl-a, Ctrl-x
builder$ pkill -f qemu-system-x86_64

# Search for qemu processes
builder$ pgrep -a qemu
builder$ ps aux | grep qemu

# Search for background jobs?
builder$ jobs

# Roll back changes
builder$ devtool undeploy-target hello-c root@localhost -P 2222
# NOTE: "successfully" undeploys targets that don't exist
```

Use `socat -,raw,echo=0,escape=0x0f UNIX-CONNECT:/tmp/ttyS0` for Ctrl-O to disconnect

Note: a container exits with the return code of the last process that it ran. This can surface what appear to be errors:

```bash
builder$
exit
2026-09-20 11:28:21 - ERROR    - Shell returned non-zero exit status
2026-09-20 11:28:21 - ERROR    - Command "/bin/bash" failed with error 1
```

when in fact it is due to the most recent command failing, for example `pgrep`:

```bash
pgrep not-a-process
echo $?
1
```

```bash
$ ./kas-container shell kas/hello.yml:kas/debug.yml
2026-09-20 11:29:42 - INFO     - kas 5.5 started on Debian GNU/Linux trixie
2026-09-20 11:29:42 - INFO     - Repository poky already contains 64e69ed23703f6358ec431d3f5f1f8483f974cae as commit
2026-09-20 11:29:42 - INFO     - Repository poky checked out to 64e69ed23703f6358ec431d3f5f1f8483f974cae
2026-09-20 11:29:42 - INFO     - To start the default build, run: bitbake -c build core-image-minimal
builder$ pgrep not-a-process
builder$
exit
2026-09-20 11:28:21 - ERROR    - Shell returned non-zero exit status
2026-09-20 11:28:21 - ERROR    - Command "/bin/bash" failed with error 1
```

ALso:

```text
```
