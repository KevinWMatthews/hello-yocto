# Hello, Yocto World

Introduction to the Yocto Project.

This builds a "Hello World" QEMU image for x86-64 using kas, which runs bitbake.

## Setup

I could install kas to system, either using apt or pip.

I am instead running it in a virtual environment using `pipx`:

```bash
pipx run kas
```

## Build

The typical build command is:

```bash
pipx run kas build hello.yml
```

but this fails on Ubuntu26. Instead use `kas-container`:

```bash
./kas-container build hello.yml
```

## Run

```bash
./kas-container shell hello.yml -c "runqemu qemux86-64 nographic slirp"
```

- slirp: user-mode networking
- tun: priviledged networking

username: `root`
password:

If logged in, exit with `poweroff` so that the kernel can tear down cleanly.

To exit QEMU, `Ctrl-a` `x`.

## Configure

TODO

```text
kas menu
kas build # optiona, somehow?
```

## Shell

To get into the container, run:

```text
./kas-container shell helo.yml
```

## Notes

I may need to invoke kas-container directly, as `./kas-container` and not `bash kas-container`.

Git worktrees generall are not fully supported.

Add `debug-tweaks` in the local conf header (appended as an entire line) so that the default password is empty.

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
