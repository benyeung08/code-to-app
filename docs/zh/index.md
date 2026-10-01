---
layout: home
title: WebToApp
titleTemplate: 在手機上構建 Android APK
hero:
  name: WebToApp
  text: 在手機上構建 Android APK
  tagline: Android 上最全功能的 web-to-app 工具包，手機上的完整 APK 工坊，現已支持原生導入和自我更新。
  actions:
    - theme: brand
      text: 快速開始
      link: /zh/guide/getting-started
    - theme: alt
      text: 在 GitHub 上查看
      link: https://github.com/benyeung08/web-to-app

features:
  - title: 13 種應用類型，一個構建器
    details: 網頁、多網頁、HTML、離線包、前端、Node.js、PHP、Python、Go、WordPress、媒體、相冊，以及全新的原生導入，支持導入現有 Android Studio 項目。
  - title: 自我更新
    details: 關於頁面現在檢查 benyeung08/web-to-app 的 Release，不再有 v1.0.0-mobilecode，乾淨的版本號，應用內下載安裝。
  - title: 設備上構建
    details: 二進制 AXML/ARSC 補丁，V1/V2/V3 簽名，AAB 導出，現在還支持通過 :native_build 進程進行完整的 Gradle 構建。
---

## 從 URL 到簽名 APK，只需三步

1. **選擇類型** 從 [13 種應用類型](/zh/guide/app-types/) 中選擇 — 普通 [網頁](/zh/guide/app-types/web) 封裝，[HTML](/zh/guide/app-types/html) 或 [前端](/zh/guide/app-types/frontend) 構建，設備上的 [Node.js](/zh/guide/app-types/nodejs)、[PHP](/zh/guide/app-types/php)、[Python](/zh/guide/app-types/python)、[Go](/zh/guide/app-types/go)、[WordPress](/zh/guide/app-types/wordpress) 服務器，或用於現有 Android Studio 項目的 [原生導入](/zh/guide/app-types/native-import)。
2. **填寫基本信息** 名稱、URL 或項目、圖標 — 然後保存。每種類型都共享相同的 [配置卡片](/zh/guide/config/)，用於網絡、隱私、外觀和運行時。
3. **構建和分享** [構建 APK](/zh/guide/app-actions/build-apk) 在設備上使用 V1/V2/V3 簽名，然後 [分享](/zh/guide/app-actions/share-apk) 或 [導出 Play-ready AAB](/zh/guide/app-actions/export-apk)。無需 PC，無需構建隊列。

## 十三種應用類型，一個構建器

[**網頁和多網頁**](/zh/guide/app-types/multi-web) URL 封裝、標籤中心、門戶和鏈接提要。

[**HTML 和離線包**](/zh/guide/app-types/html) 打包本地 HTML 或 zip 構建，或將網站抓取為自包含的離線 APK。

[**前端**](/zh/guide/app-types/frontend) 將 React、Vue 或 Vite 構建作為 localhost 服務的 APK 發布。

[**服務器運行時**](/zh/guide/app-types/nodejs) fork+exec Node.js、PHP、Python 或 Go 二進制文件，在本地端口上提供服務。

[**WordPress**](/zh/guide/app-types/wordpress) 一個完整的可移植 WordPress 網站，PHP 和 SQLite 在設備上運行。

[**媒體和相冊**](/zh/guide/app-types/media) 圖像和視頻播放器、相冊和作品集作為獨立應用。

[**原生導入（新增）**](/zh/guide/app-types/native-import) 導入現有的 Android Studio 項目並在手機上構建。第一階段：快速二進制合併 res/assets/jniLibs/dex。第二階段：通過 :native_build 進程完整執行 `./gradlew assembleDebug`，自動下載 JDK 17 + Android SDK。

## 編輯器背後的工具箱

[**Agent**](/zh/guide/more-features/agent) 一個帶有多達 57 個內置工具的工具調用助手，可以構建、編輯和操作整個應用。

[**擴展模塊**](/zh/guide/more-features/extension-modules) 將 JS/CSS、用戶腳本或 MV3 Chrome 擴展注入任何生成的應用。

[**Hosts 廣告屏蔽**](/zh/guide/more-features/hosts-adblock) 20 個內置過濾列表和按應用訂閱，編譯到發布的 APK 中。

[**Linux 環境**](/zh/guide/more-features/linux-environment) 一個類似 Termux 的環境，帶有真正的工具鏈，用於構建和運行項目。

[**端口管理器**](/zh/guide/more-features/port-manager) 每個本地服務器運行時的衝突策略、真正的停止處理程序和 DNS 橋接。

[**應用修改器**](/zh/guide/more-features/app-modifier) 克隆和重塑已安裝的 APK，批量導入定義，導出模板。

[**自我更新**](/zh/guide/more-features/self-update) 新增：關於頁面檢查 benyeung08/web-to-app 的發行版，下載 APK 到 update_apks/，通過 FileProvider 安裝。
