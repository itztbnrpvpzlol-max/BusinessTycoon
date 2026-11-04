# Aether Launcher

A native Android launcher project for Minecraft: Java Edition.

## Current stage

This first milestone replaces the old Tidebound prototype with a native Jetpack Compose launcher shell. It includes a version selector, profile overview, and device-focused performance settings UI.

**Important:** The current APK is a launcher interface prototype. It does not yet contain a Java runtime, Minecraft downloader, Microsoft sign-in, or a game renderer, so the Play action is intentionally unavailable.

## Engineering direction

The project will keep the Android UI separate from the game execution layer. Running Java Edition on Android requires more than a normal Android Java process: it needs compatible OpenJDK builds, an Android-adapted window/input layer, native libraries, audio, and a graphics translation/backend. We will build and test those pieces as distinct modules rather than claiming the UI itself can launch the game.

## Planned milestones

1. Native launcher UI and saved profiles.
2. Version metadata, asset/library download and integrity checks.
3. Java runtime management for supported Android ABIs.
4. Android-compatible window, input, audio and graphics backend.
5. Microsoft account authentication and secure token storage.
6. Fabric/Forge support and per-instance settings.

## Build

GitHub Actions runs gradle assembleDebug and uploads the debug APK as the AetherLauncher-debug-apk artifact.
