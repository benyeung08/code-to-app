# 原生導入

> 導入現有的 Android Studio 項目並在手機上構建 — 無需電腦。

這是在 `benyeung08/web-to-app` fork 中新增的第 13 種應用類型。

## 功能

你有一個現有的 Android Studio 項目（包含 `app/src/main/AndroidManifest.xml`）。將其壓縮為 zip 或選擇其文件夾，WebToApp 將在設備上構建簽名的 APK。

## 兩個階段

### 第一階段 - 快速二進制合併（推薦，今晚就能用）

不需要 Gradle，使用 WebToApp 現有的二進制補丁：

1. `NativeProjectImporter` 驗證結構
2. 複製 `src/main/res`、`assets`、`jniLibs` 到 `files/native_import/{id}/`
3. `NativeImportApkBuilder` 二進制合併並重簽名

秒出 APK。

### 第二階段 - 完整 Gradle 構建（真正編譯）

複用 WebToApp 的 fork+exec 架構（與 Node.js 的 `:nodejs` 進程相同）：

- `gradle_launcher` C++ 包裝器
- 自動下載 OpenJDK 17 + Android SDK + Gradle 8.7
- 在 `:native_build` 進程中執行 `./gradlew assembleDebug`
- 前台服務顯示通知和實時日誌

需要 6GB+ RAM。

## 使用方法

1. **創建** → **原生導入**（第 13 張卡片）
2. 選擇 ZIP 或文件夾
3. 解析器顯示包名、資源數量、.so 數量
4. 點擊快速構建或完整構建

## 自我更新配套

此功能與自我更新修復一起發布，關於頁面的 `v1.0.0-mobilecode` 將變為 `v1.0.1`，點擊刷新圖標會去 `benyeung08/web-to-app` 檢查更新。
