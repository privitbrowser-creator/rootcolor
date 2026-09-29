# ColorRoot

Root Android app for device-specific display color enhancement.

## Important
Android/OEM kernels do not provide one universal root interface for saturation,
contrast, or color temperature. ColorRoot therefore discovers writable
`sysfs` nodes and changes only nodes explicitly named `saturation`, `contrast`,
or `color_temperature`.

Before the first change it saves the original values to:
`/data/local/tmp/colorroot_backup.txt`

Use **Normal — Restore** to return those saved values.

If the device exposes no compatible nodes, the app reports that instead of
blindly modifying unrelated kernel parameters.

## Build
Use GitHub Actions workflow:
`.github/workflows/build_apk.yml`

The generated debug APK is an installable test build. Root management is
expected to be provided by Magisk/KernelSU or another `su` implementation.
