# Horizon Android integration spike

This branch starts the JNI boundary for the Horizon Android port. It does **not** yet add a selectable, playable engine backend.

## What is here

`ZalithLauncher/src/main/java/net/bycesardev/horizon/HorizonNativeBridge.kt` declares the JNI methods exported by `crates/horizon-android` in [ByCesarDev/horizon-android](https://github.com/ByCesarDev/horizon-android), and provides guarded Kotlin calls for initialization, surface lifecycle, touch, and Activity pause/resume/destroy. If `libhorizon_android.so` is absent, the launcher continues to work and the bridge reports unavailable rather than crashing at class load.

The native export names are tied to the `net.bycesardev.horizon.MainActivity` JNI class name. Keep the shim class and method names aligned with upstream when updating the engine.

## Remaining work before this can be tried as an engine

- Build and package a compatible `libhorizon_android.so` for every supported ABI. The upstream Android sample currently documents only `arm64-v8a`; Zalith supports additional ABI variants.
- Provide Horizon's expected Minecraft resources in its private resource layout, with appropriate licensing and ownership checks. Zalith's current component/runtime store is not automatically Horizon's resource format.
- Add an explicit engine selection and launch flow. Zalith's current renderer selection only selects OpenGL/EGL libraries for a Java Minecraft JVM; it cannot switch to Horizon's independent Rust client.
- Host the native surface in the game Activity and route surface replacement, resize, lifecycle, touch, keyboard, and controller events without disrupting existing Java/LWJGL input.
- Verify upstream readiness first: the Android crate source describes its Android entry loop as a minimal startup/render path, with connection to the complete game loop still future work. This bridge alone cannot make Minecraft playable.
- Add instrumentation/device tests, validate Vulkan/GLES behavior, and test shutdown/recreation on real Android hardware.

No third-party native binaries are copied or downloaded by this change. Do not mark the engine as available or expose a play button until the native artifact, resource preparation, and end-to-end game path are implemented and tested.
