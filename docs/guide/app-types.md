---
title: 应用类型
---

# 应用类型

CodeToApp 一共 13 种应用类型。选错也不用担心，创建后随时可以改核心配置。

## WebView 承载类

### 网页（WEB）
最基础的网址套壳。填一个 URL，配上图标和名称就能用。

### 多站点（MULTI_WEB）
标签式聚合门户：把多个站点放进同一个应用，用底部标签切换。支持分站独立配置。

### HTML（HTML）
打包本地 HTML 文件，适合单页作品、说明文档、小工具。

### 离线包（OFFLINE）
把线上站点抓取成一个自包含的离线 APK，断网也能打开。

### 前端（FRONTEND）
把 React、Vue 或 Vite 的构建产物（`dist/`）做成一个 localhost 服务的 APK。

### 相册（GALLERY）
图片与视频的集合，支持网格/幻灯片播放、分类、转场间隔。

## 原生运行时类

::: warning 关于 targetSdk
以下类型通过 fork+exec 从应用私有存储启动原生二进制。
从 `targetSdk 29` 起，Android 对可写数据目录强制执行 W^X（write-xor-execute），
会阻断对内置二进制的 execve。因此这些类型必须保持 `targetSdk <= 28`。
:::

### Node.js（NODEJS_APP）
运行一个 Node 项目，在设备上开端口提供服务，用 WebView 打开。

### PHP（PHP_APP）
运行 PHP 项目，自带 PHP 二进制。

### Python（PYTHON_APP）
运行 Python 项目，支持 pip 依赖与自定义扩展。

### Go（GO_APP）
编译并运行 Go 二进制。

### WordPress（WORDPRESS）
一个完整可移植的 WordPress 站点，PHP + SQLite 直接在设备上跑起来。

## 通用源码类

### CodeToApp（CODETOAPP）

::: tip 本版本新增
这是 CodeToApp 相对上游新增的类型。
:::

与上面那些「单一语言运行时」不同，CodeToApp 类型是一个**通用源码项目容器**：

1. 你导入一个源码目录（或 Git 仓库）
2. 应用自动侦测语言与框架 —— 依据 `package.json` / `requirements.txt` / `go.mod` / `index.php` 等特征文件
3. 侦测结果写进 `detectedRuntime`，再**委派给对应的既有运行时启动器**

| 配置项 | 说明 |
| --- | --- |
| `sourcePath` | 源码目录（应用私有存储内的绝对路径） |
| `detectedRuntime` | 侦测结果：`NODEJS` / `PYTHON` / `GO` / `PHP` / `STATIC` |
| `entryFile` | 入口文件 |
| `serverPort` | 服务端口，0 表示自动分配 |
| `buildCommand` | 构建命令 |
| `startCommand` | 启动命令 |
| `envVars` | 环境变量 |
| `staticDir` | 静态资源目录 |

未侦测到运行时时，行为默认贴近 **前端** 类型（本地源码 + WebView 承载）。

## 选择建议

| 你的场景 | 推荐类型 |
| --- | --- |
| 只是想把一个网站变成 App | 网页 |
| 多个站点聚合到一个 App | 多站点 |
| 已有 React/Vue 构建产物 | 前端 |
| 有源码但不确定语言 | **CodeToApp** |
| 断网环境要用 | 离线包 |
| 需要跑真实后端服务 | Node.js / PHP / Python / Go |
