---
title: 导出流水线
---

# 导出流水线

导出是这个项目最复杂的部分：它要在 Android 应用内部，完成通常在 PC 上用 `aapt2` + `apksigner` 做的事。

## 为什么不用远程构建

CodeToApp **没有**远程构建队列。所有二进制改写、签名、对齐都在设备上完成，好处是：

- 离线可用
- 源码不离开设备
- 没有队列等待

代价是首次构建较慢（要初始化运行时与外壳模板）。

## 关键组件

| 组件 | 职责 |
| --- | --- |
| `ApkBuilder` | 总调度：配置 → 外壳 → 输出 |
| `AxmlEditor` | 二进制 AXML 改写（包名、应用名） |
| `AxmlRebuilder` | 重建 manifest，注入 intent-filter |
| `ArscEditor` | 资源表改写 |
| `ApkExportPreflight` | 导出前检查 |
| `ExportRuntimeEnsure` | 确保运行时依赖就位 |
| `ProjectDirCleaner` | 清理构建中间产物 |
| apksig | V1/V2/V3 签名 |

## 二进制 AXML 改写

AndroidManifest 在 APK 里是**编译后的二进制格式**，不是 XML 文本。所以不能做字符串替换，必须：

1. 解析 binary AXML 的 chunk 结构
2. 在字符串池里定位 `package` 属性
3. 改写字符串并重建池
4. 重新计算 chunk 偏移与长度

`AxmlEditor` 里的 `ORIGINAL_PACKAGE` 常量就是这一步要匹配的目标串。

::: danger FAIL-LOUD 设计
如果在 manifest 里找不到目标包名字符串，`AxmlEditor` 会**直接抛异常**，
而不是静默产出一个坏包。这是刻意的：静默失败会产出一个看起来能装、但运行时崩溃的 APK。
:::

## 权限裁剪

外壳模板为了通用性声明了较宽的权限集。导出时会按应用实际配置裁剪掉用不到的权限，
降低杀软误报概率。

## 资源加密

可选的 AES-256-GCM 加密：把注入的 JS/CSS/HTML 资源加密后打包，运行时解密。
用于保护你不希望被轻易提取的注入脚本。

## 16 KB 页对齐

新版 Android 要求原生库按 16 KB 页对齐。导出流水线会自动处理 `.so` 的对齐，
不满足时给出警告。

## 签名

用内置 apksig 完成：

- **V1**（JAR 签名）—— 兼容老设备
- **V2**（APK 签名方案 v2）—— Android 7+
- **V3** —— Android 9+，支持密钥轮换

## 调试技巧

导出失败时，先看 `ApkExportPreflight` 的报错。它会明确指出缺什么，
而不是笼统地说「导出失败」。

常见的一类失败是外壳模板被改坏 —— 检查 `:shell` 模块的 `applicationId` 是否还是 `com.webtoapp`。
