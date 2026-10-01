---
layout: home
title: WebToApp
titleTemplate: Build Android APKs on your phone
hero:
  name: WebToApp
  text: Build Android APKs on your phone
  tagline: The most full featured web-to-app toolkit on Android, a complete APK workshop that runs entirely on your phone. Now with Native Import & self-update.
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: View on GitHub
      link: https://github.com/benyeung08/web-to-app
  image:
    src: /logo.png
    alt: WebToApp

features:
  - title: 13 App Types, One Builder
    details: Web, Multi-Web, HTML, Offline Pack, Frontend, Node.js, PHP, Python, Go, WordPress, Media, Gallery, and NEW Native Import for existing Android Studio projects.
  - title: Self-Update
    details: About screen now checks benyeung08/web-to-app releases. No more v1.0.0-mobilecode — clean versioning with in-app download & install.
  - title: On-Device Build
    details: Binary AXML/ARSC patching, V1/V2/V3 signing, AAB export, and now full Gradle build via :native_build fork+exec.
---

## From URL to a signed APK in three steps

1. **Pick a type** Choose from [13 app types](/guide/app-types/) — a plain [Web](/guide/app-types/web) wrapper, [HTML](/guide/app-types/html) or [Frontend](/guide/app-types/frontend) builds, on-device [Node.js](/guide/app-types/nodejs), [PHP](/guide/app-types/php), [Python](/guide/app-types/python), [Go](/guide/app-types/go), [WordPress](/guide/app-types/wordpress) servers, or [Native Import](/guide/app-types/native-import) for existing Android Studio projects.
2. **Fill in the basics** A name, a URL or a project, an icon — then save. Every type shares the same [configuration cards](/guide/config/) for network, privacy, appearance, and runtimes.
3. **Build and share** [Build APK](/guide/app-actions/build-apk) signs on-device with V1/V2/V3, then [share](/guide/app-actions/share-apk) it or [export a Play-ready AAB](/guide/app-actions/export-apk). No PC, no build queue.

## Thirteen app types, one builder

[**Web and Multi-Web**](/guide/app-types/multi-web) URL wrappers, tabbed hubs, portals, and link feeds.

[**HTML and Offline Pack**](/guide/app-types/html) Package local HTML or zip builds, or scrape a site into a self-contained offline APK.

[**Frontend**](/guide/app-types/frontend) Ship React, Vue, or Vite builds as a localhost-served APK.

[**Server runtimes**](/guide/app-types/nodejs) fork+exec Node.js, PHP, Python, or Go binaries that serve on a local port.

[**WordPress**](/guide/app-types/wordpress) A full portable WordPress site with PHP and SQLite running on-device.

[**Media and Gallery**](/guide/app-types/media) Image and video players, albums, and portfolios as standalone apps.

[**Native Import (NEW)**](/guide/app-types/native-import) Import an existing Android Studio project and build it on your phone. Phase 1: fast binary merge of res/assets/jniLibs/dex. Phase 2: full `./gradlew assembleDebug` via :native_build process with JDK 17 + Android SDK auto-download.

## A toolbox behind the editor

[**Agent**](/guide/more-features/agent) A tool-calling assistant with up to 57 built-in tools that can build, edit, and operate the whole app.

[**Extension modules**](/guide/more-features/extension-modules) Inject JS/CSS, userscripts, or MV3 Chrome extensions into any generated app.

[**Hosts ad-block**](/guide/more-features/hosts-adblock) 20 built-in filter lists and per-app subscriptions, compiled into the shipped APK.

[**Linux environment**](/guide/more-features/linux-environment) A Termux-style environment with real toolchains for building and running projects.

[**Port manager**](/guide/more-features/port-manager) Conflict policies, real stop handlers, and DNS bridging for every local server runtime.

[**App modifier**](/guide/more-features/app-modifier) Clone and rebrand installed APKs, batch-import definitions, export templates.

[**Self-update**](/guide/more-features/self-update) NEW: About screen checks benyeung08/web-to-app releases, downloads APK to update_apks/, installs via FileProvider.

## Under the hood

13 app types
57 agent tools max
10 UI languages
20 ad-filter lists

The builder does its own binary patching — AXML/ARSC rewriting, permission pruning, AES-256-GCM resource encryption, 16 KB page-aligned native libraries — and keeps a low targetSdk shell so fork+exec runtimes keep working. Phase 2 adds a `gradle_launcher` native wrapper for true on-device Gradle builds. The [developer docs](/developer/architecture) cover the full export pipeline.

Ready to build your first APK? [Get started](/guide/getting-started)
