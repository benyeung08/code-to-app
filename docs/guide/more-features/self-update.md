# Self-update

> The About screen now self-updates from your own repo.

## Problem in screenshot

Original fork shows `v1.0.0-mobilecode · 1` with refresh icon. That's MobileCode IDE injecting suffix, and `UpdateChecker.kt` pointing to `shiaho777/web-to-app`.

## Fix

1. `app/src/main/java/com/webtoapp/core/update/UpdateChecker.kt`:
   ```kotlin
   const val OWNER = "benyeung08"
   const val REPO = "web-to-app"
   ```
   - `checkOwnUpdate()` fetches `https://api.github.com/repos/benyeung08/web-to-app/releases/latest`
   - `pickBestApkAsset()` prefers `standardRelease` APK
   - `downloadApk()` to `getExternalFilesDir("update_apks")`
   - `installApk()` via FileProvider

2. `app/build.gradle.kts`:
   ```kotlin
   versionCode = 2
   versionName = "1.0.1"
   buildTypes { release { versionNameSuffix = "" } }
   productFlavors { standard { versionNameSuffix = "" } }
   ```

3. `app/src/main/AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
   <provider android:authorities="${applicationId}.fileprovider" ... />
   ```

4. `app/src/main/res/xml/filepaths.xml`:
   ```xml
   <external-files-path name="update_apks" path="update_apks/" />
   ```

5. `.github/workflows/release.yml` builds APK on tag `v*` and uploads to Release.

After that, tap refresh icon in About → dialog shows changelog → Install.
