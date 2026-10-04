# Native Import

> Import an existing Android Studio project and build it on your phone — no PC needed.

This is the 13th app type added in `benyeung08/web-to-app` fork.

## What it does

You have an existing Android Studio project (with `app/src/main/AndroidManifest.xml`). You zip it or select its folder, WebToApp will build a signed APK on-device.

## Two phases

### Phase 1 - Fast binary merge (recommended, works tonight)

No Gradle needed. Uses WebToApp's existing binary patching:

1. `NativeProjectImporter` validates structure
   - Must have `app/src/main/AndroidManifest.xml`
   - Detects package, minSdk, Kotlin, .so files
2. Copies `src/main/res`, `assets`, `jniLibs`, `java/kotlin` to `files/native_import/{id}/`
3. `NativeImportApkBuilder`:
   - Unzips base `webview_shell.apk`
   - AXML patch Manifest package
   - ARSC patch + overlay res
   - Copy `lib/arm64-v8a/*.so`
   - Merge dex via d8 (`classes.jar` -> `classes2.dex`)
   - Re-sign V1/V2/V3 via `apksig`

Result: APK in seconds.

### Phase 2 - Full Gradle build (true compilation)

Reuses WebToApp's fork+exec architecture (same as Node.js `:nodejs` process):

- `gradle_launcher` C++ wrapper (`app/src/main/cpp/gradle_launcher/`)
  ```cpp
  pid = fork();
  if (pid==0) {
    setenv("JAVA_HOME", jdkDir);
    setenv("ANDROID_HOME", sdkDir);
    chdir(projectRoot);
    execv("./gradlew", {task});
  }
  ```
- `NativeBuildRuntime` + `GradleInstaller`:
  - Auto-downloads OpenJDK 17 arm64 (~180MB), Android cmdline-tools, Gradle 8.7
  - Stored in `files/toolchain/`
  - Writes `local.properties` with `sdk.dir`
- `NativeBuildService` (`:native_build` process):
  - Foreground service with notification
  - Real-time log to UI

Requires 6GB+ RAM.

## How to use

1. **Create** → **Native Import** (13th card)
2. Choose ZIP or Folder (SAF)
3. Parser shows package, res count, .so count
4. Click:
   - **Fast Build** → Phase 1
   - **Full Build** → Phase 2 (first time downloads toolchain)

## File locations

- `app/src/main/java/com/webtoapp/core/importer/NativeProjectImporter.kt`
- `app/src/main/java/com/webtoapp/core/apk/NativeImportApkBuilder.kt`
- `app/src/main/java/com/webtoapp/ui/screens/NativeImportScreen.kt`
- `app/src/main/cpp/gradle_launcher/gradle_launcher.cpp`
- `app/src/main/java/com/webtoapp/core/runtime/GradleInstaller.kt`

## Self-update companion

This feature ships with self-update fix:

- `UpdateChecker.kt` owner changed from `shiaho777` to `benyeung08`
- `build.gradle.kts` `versionNameSuffix = ""` removes `v1.0.0-mobilecode`
- `AndroidManifest.xml` adds `REQUEST_INSTALL_PACKAGES` + FileProvider
- `.github/workflows/release.yml` builds APK on tag `v*`
