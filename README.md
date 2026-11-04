# EffectCraft Mobile

An Android-focused mobile port project for [EffectCraft](https://github.com/storytold/effectcraft), an open-source motion graphics and compositing application.

## Direction

This repository is now focused on one app: **EffectCraft Mobile**. The previous Aether Launcher / Minecraft launcher product direction has been retired from the Android app.

The goal is to bring EffectCraft's actual composition, layer, keyframe, effects, preview and export engine to Android with a touch-first Kotlin interface.

## Current state

- Native Android application shell using Kotlin and Jetpack Compose.
- Initial touch-oriented editor layout with composition viewer placeholder, layer list, timeline controls and property-panel entry points.
- ARM APK packaging configured for armeabi-v7a and arm64-v8a, including a universal APK.
- The upstream EffectCraft engine is not yet linked into the Android app. The current viewer and timeline are UI scaffolding, not a functioning renderer or editor.
- No WebView-based hosted editor is used.

## Port architecture

- **Android UI:** Kotlin + Jetpack Compose.
- **Core engine:** upstream EffectCraft Rust crates, integrated incrementally.
- **Native bridge:** JNI / Rust Android library once the engine's Android-compatible crate graph is established.
- **Rendering:** investigate the existing wgpu/GLES/Vulkan paths against actual Android GPU support.
- **Projects:** preserve EffectCraft's .ecproj project format and upstream serialization.
- **ABI targets:** armeabi-v7a and arm64-v8a. A universal APK is produced for convenient installation.

The upstream project is dual-licensed MIT or Apache-2.0. Preserve its NOTICE, license files and ATTRIBUTION.md; third-party assets and dependencies retain their own licenses. The ArtCraft / EffectCraft trademarks and logos are not granted by the source license, so this mobile fork uses its own app identity and must not reuse protected brand assets outside the upstream terms.

## Device constraints

The initial target includes lower-end Android devices. Rendering must be profiled on real devices, use conservative preview resolution and cache budgets, and provide a lightweight mode. ARM CPU support alone does not imply GPU feature support. The 32-bit ARM build must be tested separately; unsupported dependencies or APIs must be excluded or replaced before claiming compatibility.

## Build

GitHub Actions builds the Android debug APK. Artifacts include ABI-specific packages and the universal APK.

## Upstream

- Source: https://github.com/storytold/effectcraft
- Architecture: https://github.com/storytold/effectcraft/blob/main/docs/architecture.md
- Web build notes: https://github.com/storytold/effectcraft/blob/main/docs/web.md
- Licenses: MIT or Apache-2.0 (see upstream LICENSE-MIT, LICENSE-APACHE, NOTICE, and ATTRIBUTION.md).

## Honest status

This is the beginning of the Android port, not yet a complete After Effects replacement. The native editor interface is only a shell until the Rust project model, renderer, effects, footage pipeline, audio and export engine are successfully integrated and tested on Android.
