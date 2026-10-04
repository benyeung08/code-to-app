---
title: 开发者概览
---

# 开发者文档

## 技术栈

| 层 | 技术 |
| --- | --- |
| UI | Jetpack Compose（Material 3） |
| 语言 | Kotlin 2.x |
| 构建 | Gradle (Kotlin DSL) + KSP |
| 数据库 | Room |
| 原生 | CMake + NDK（C/C++） |
| 序列化 | Gson + ProtoBuf |
| 网络 | OkHttp / Chromium 网络栈 |
| 文档站 | VitePress 1.6 + Vue 3 |

## 模块结构

```text
:app            主应用（com.codetoapp.app）
:shell          导出的 APK 外壳模板（com.webtoapp）
:clone-host     克隆功能宿主
:feature-stacks 功能模块（admob 等）
:modules        插件模块
```

::: warning 外壳包名不要改
`:shell` 的 `applicationId` 必须保持 `com.webtoapp`。
`ApkBuilder` 靠这个字符串在 AXML 里做包名替换。
改了它，导出 APK 时会直接 FAIL-LOUD 抛「Package name string not found in manifest」。
:::

## 本地构建

```bash
./gradlew :app:assembleStandardDebug \
  -PallowDebugSignedRelease=true \
  -PskipShellTemplateSync=true \
  -PskipStackBundlesSync=true
```

| 参数 | 作用 |
| --- | --- |
| `-PallowDebugSignedRelease=true` | 允许用 debug 签名构建 release 变体（CI 必需） |
| `-PskipShellTemplateSync=true` | 跳过外壳模板同步，加快构建 |
| `-PskipStackBundlesSync=true` | 跳过运行时 bundle 同步 |

## 测试

```bash
./gradlew :app:testStandardDebugUnitTest
```

## 文档站本地预览

```bash
cd docs
npm install
npm run dev
```

## 下一步

- [导出流水线](/developer/export-pipeline)
- [架构](/developer/architecture)
