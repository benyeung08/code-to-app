---
title: 构建与导出
---

# 构建与导出

## 导出前检查

CodeToApp 在导出前会跑一遍 preflight，检查：

- 运行时依赖是否已下载（`ExportRuntimeEnsure`）
- 权限声明是否与配置一致
- 资源是否完整
- AXML/ARSC 改写目标是否存在

任何一项不过，导出会在这一步停下并给出原因，**不会**产出一个坏包。

## 导出流水线

```text
源码 / URL
   ↓
配置序列化（ApkConfigJson）
   ↓
外壳模板同步（shell APK）
   ↓
二进制 AXML / ARSC 改写
   ├─ 包名替换
   ├─ 应用名替换
   ├─ 权限裁剪
   └─ intent-filter 注入（分享 / 打开方式）
   ↓
资源加密（AES-256-GCM，可选）
   ↓
原生库 16 KB 页对齐
   ↓
V1 / V2 / V3 签名（apksig）
   ↓
zipalign
   ↓
APK / AAB
```

## 签名

| 方案 | 适用 |
| --- | --- |
| Debug 签名 | 本地测试，安装快，但不能覆盖升级别的签名版本 |
| 自定义 keystore | 正式分发，升级时签名必须一致 |

::: warning 签名一致性
Android 要求覆盖升级时签名一致。debug 签名的包和 release 签名的包**互相无法覆盖**，
必须先卸载旧版。
:::

## AAB 导出

Google Play 需要 AAB。在导出界面选 **AAB**，流程与 APK 相同，最后一步用 apksig 转成 bundle。

::: tip Play 政策检查
导出前会检查应用是否声明了 fork+exec 运行时。
带了原生运行时的应用必须保持低 `targetSdk`，Play 政策检查器会在导出前提示这一点。
:::

## 产物位置

```
内部存储/Android/data/com.codetoapp.app/files/export/
```

## 克隆与改名

除了从零构建，CodeToApp 也能：

- **克隆已安装的 APK** —— 读取目标包名，替换包名与应用名后重新签名
- **批量导入定义** —— 一次导入多个应用配置
- **导出模板** —— 把当前配置存成模板复用

## 常见失败原因

| 报错 | 原因 |
| --- | --- |
| `Package name string not found in manifest` | 外壳模板的包名被改过，导致 AXML 改写找不到目标串 |
| `Release build has no valid signing config` | 未配置签名。CI 下需传 `-PallowDebugSignedRelease=true` |
| `execve failed` | `targetSdk` 高于 28，W^X 阻断了原生二进制执行 |
| 16 KB 对齐警告 | 原生库未按 16 KB 页对齐，导出流水线会自动处理 |
