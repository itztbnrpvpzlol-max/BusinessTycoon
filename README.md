# Aether Launcher

A native Android launcher project for Minecraft: Java Edition.

## Current stage

Aether currently contains a native Jetpack Compose launcher interface with:
- Minecraft profile/version selection plus an on-demand catalog of the latest 40 stable releases from Mojang's official version manifest (metadata only; no game download yet).
- Locally persisted memory, render-distance and FPS preferences.
- A conservative low-end preset (512 MB configured Java heap, 2 chunks, 30 FPS).
- Device diagnostics for Android version, total RAM, Android app memory class, current app-process bitness, supported ABIs and Android-reported OpenGL ES capability.
- A live EGL/OpenGL ES 2.0 context probe that reads the graphics version, renderer and vendor from the installed driver.
- Home, Instances and Settings navigation.

The low-end preset is a starting configuration for testing on the developer's 2 GB Android 14 phone with a Spreadtrum SC9832E CPU and Mali-T820 GPU. It is not a performance guarantee. The app reports the runtime ABI and does not assume that an ARMv8 CPU means the installed Android userspace is 64-bit.

**Important:** This is still a launcher foundation, not a playable Minecraft launcher. The Java runtime, Minecraft version metadata/download pipeline, asset and library downloads, authentication, Android window/input/audio integration, and graphics backend are not connected. The Play action remains disabled until a real execution pipeline exists.

## Engineering direction

Keep Android UI/profile management separate from the game execution layer. Investigate compatible OpenJDK builds and an Android graphics/window/input bridge before selecting native binaries. Validate ABI and OpenGL ES support on real devices; do not ship an ABI or GPU assumption based only on the CPU name. The app's EGL probe is a diagnostic (it creates a tiny off-screen ES 2 context), not a benchmark or proof that a Minecraft renderer will work. MobileGlues is one candidate graphics layer, but its published requirement is OpenGL ES 3.0 minimum (3.2 recommended), so Aether must check the actual reported GLES version before selecting it.

## Planned milestones

1. Native launcher UI, saved profiles and device diagnostics.
2. Version manifest, asset/library download and integrity verification.
3. Compatible Java runtime management for detected Android ABIs.
4. Android-compatible window, input, audio and graphics backend.
5. Microsoft account authentication and secure token storage.
6. Fabric/Forge support and per-instance settings.

## Build

GitHub Actions runs `gradle assembleDebug` and uploads the debug APK as the `AetherLauncher-debug-apk` artifact.
