# 自我更新

> 關於頁面現在從你自己的倉庫自我更新。

## 截圖中的問題

原 fork 顯示 `v1.0.0-mobilecode · 1`，這是 MobileCode IDE 注入的後綴，且 `UpdateChecker.kt` 指向 `shiaho777/web-to-app`。

## 修復

1. `UpdateChecker.kt` owner 改為 `benyeung08`
2. `build.gradle.kts` 強制 `versionNameSuffix = ""` 去掉 `-mobilecode`
3. `AndroidManifest.xml` 加安裝權限和 FileProvider
4. `.github/workflows/release.yml` 打 tag 自動發版

之後點關於頁的刷新圖標就會彈出更新對話框。
