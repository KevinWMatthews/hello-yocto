# Hello, Yocto World

Introduction to the Yocto Project.

This builds a "Hello World" QEMU image for x86-64 using kas, which runs bitbake.

## Getting Started

### Setup

`kas` and `bitbake` are tested on specific Ubuntu versions.

To work around this, these instructions recommend containerizing the build using
[kas-container](https://github.com/siemens/kas/blob/master/kas-container).

TODO Add command for downloading this.

If your system is compatible, consider running `kas` commands using `pipx`:

TODO Add command for installing `pipx` to system.

### Build

Build using `kas-container`:

```bash
# TODO Update to include bind mount for local external library
# TODO Push repo upstream
./kas-container build hello.yml
```

### Run

```bash
./kas-container shell hello.yml -c "runqemu qemux86-64 nographic slirp"
```

- slirp: user-mode networking
- tun: priviledged networking

username: `root`
password:

If logged in, exit with `poweroff` so that the kernel can tear down cleanly.

To exit QEMU, `Ctrl-a` `x`.

### Configure

TODO rewrite to use kas-container

```bash
kas menu
kas build
```

## Customizing the System

### Add a Service

TODO Learn/relearn how to create a recipe and add a layer.

### Development Flow

Ideally, much application development will occur in the external repo.

When libraries are to be developed in the container, it is possible to build a single recipe:

```bash
bitbake hello-in-tree
bitbake -c hello-in-tree
```

## Additional Information

### kas-container

`kas-container` creates a container with dependencies and build-related programs installed.

To shell into the build container, run:

```bash
./kas-container shell hello.yml
```

### Alternate Build Instructions

If `kas` is installed to system, build commands can be run directly:

```bash
kas build hello.yml
```

Alternatively, `pipx` can manage the installation of `kas`:

```bash
pipx run kas build hello.yml
```

### Bitbake Layers

These commands can be run after shelling into the `kas` container.

```bash
bitbake-layers --help
bitbake-layers show-layers
bitbake-layers show-recipes # there are many!
bitbake-layers show-appends

bitbake-layers create-layer
# Usually avoid tdhis, working with kas instead
bitbake-layers add-layer
```

Be wary of creating a layer from within `build/`!
This "hides" the generated config files in the build directory.

### Recipes

Once source is in place, scaffold recipes with:

```bash
devtool add hello-cmake /path/to/hello-source
```

Then edit the `.bb` file in `workspace/`.

When editing the source, it _can't_ be in the configuration tree by the recipe!
It must live in a separate directory, say in `src/`. Then you can build it with:

```bash
devtool build hello-cmake
```

If just compiling, try:

```bash
bitbake -c compile hello-cmake
```

This doesn't install the binary to the image (it only runs `do_build`) so be sure to run `devtool build hello-cmake` again to run the `do_install` step.

Once things work, commit this recipe using:

```bash
devtool finish hello-cmake ../meta-hello
```

This moves the recipe from `workspace/` into the tree itself.

## External repo

Trying with a git URI that points to disk.
Must mount this when running the container:

```bash
# Test mount
./kas-container --runtime-args "-v ../hello_c:/work/src/hello_c:ro" shell hello.yml
# Mount during build
./kas-container --runtime-args "-v ../hello_c:/work/src/hello_c:ro" build hello.yml
# Run as usual
./kas-container shell hello.yml -c "runqemu qemux86-64 nographic slirp"
```

TODO I should be able to add a recipe to devtool, right?

It seems so:

```bash
devtool add hello-c /work/src/hello_c
devtool build hello-c
# apparently?
mkdir -p path/to/meta-hello/recipes-hello/hello-c
devtool finish hello-c ../meta-hello
```

TODO Should be possible to unpack the source locally with:

```bash
devtool modify hello-c
INFO: Source tree extracted to /work/build/workspace/sources/hello-c
INFO: Recipe hello-c now set up to build from /work/build/workspace/sources/hello-c
```

Then build and test:

```bash
devtool build hello-c
# might need this to install?
bitbake core-image-minimal
runqemu qemux86-64 nographic slirp
```

prefer to try this - requires a standalone qemu already running:

```text
devtool build hello-c
devtool deploy-target hello-c root@<target>
```

```text
find /work/build/tmp/work/*/core-image-minimal/*/rootfs/ -name hello
```

## Notes

I may need to invoke kas-container directly, as `./kas-container` and not `bash kas-container`.

Git worktrees generall are not fully supported.

Add `debug-tweaks` in the local conf header (appended as an entire line) so that the default password is empty.

Layers are typically prefixed with `meta-`.

### Errors

Ubuntu 2026 is not yet supported:

```text
$ pipx run kas build hello.yml
2026-08-27 22:38:05 - INFO     - kas 5.5 started on Ubuntu resolute
2026-08-27 22:38:05 - INFO     - Repository poky already contains 64e69ed23703f6358ec431d3f5f1f8483f974cae as commit
2026-08-27 22:38:05 - INFO     - Repository poky checked out to 64e69ed23703f6358ec431d3f5f1f8483f974cae
2026-08-27 22:38:05 - INFO     - /home/kevin/src/yocto/hello_world/build$ /home/kevin/src/yocto/hello_world/poky/bitbake/bin/bitbake -c build core-image-minimal
WARNING: Host distribution "ubuntu-26.04" has not been validated with this version of the build system; you may possibly experience unexpected failures. It is recommended that you use a tested distribution.
ERROR: User namespaces are not usable by BitBake, possibly due to AppArmor.
See https://discourse.ubuntu.com/t/ubuntu-24-04-lts-noble-numbat-release-notes/39890#unprivileged-user-namespace-restrictions for more information.

Summary: There was 1 WARNING message.
Summary: There was 1 ERROR message, returning a non-zero exit code.
2026-08-27 22:38:06 - ERROR    - Command "/home/kevin/src/yocto/hello_world/poky/bitbake/bin/bitbake -c build core-image-minimal" failed with error 1
```

The solution seems to be to run a build using the `kas-container` script provided in <https://github.com/siemens/kas>.

## TODO

Create a workflow for building from source locally but in the kas container.
Then learn to build from the upstream repo by default

Create a hello-world Rust app.
