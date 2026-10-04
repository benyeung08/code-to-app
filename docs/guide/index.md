---
title: 简介
---

# CodeToApp 是什么

CodeToApp 是一个跑在 Android 设备上的 APK 工坊。它的能力远不止「把一个网址套进 WebView」—— 它能把真实的服务器运行时、加固的网络栈、以及完整的二进制打包流水线，全部塞进你手机里。

## 和「网址套壳」有什么不同

普通的 WebView 套壳工具只能做一件事：打开一个 URL。CodeToApp 能做的是：

| 能力 | 说明 |
| --- | --- |
| 原生运行时 | Node.js、PHP、Python、Go 以原生二进制 fork+exec 启动，在设备上开端口提供服务 |
| 网络加固 | DoH、TLS 指纹伪装、ECH、按应用代理、CORS 绕过 |
| 二进制打包 | AXML/ARSC 二进制改写、权限裁剪、V1/V2/V3 签名、AAB 导出 |
| 插件体系 | JS/CSS 模块、用户脚本、MV3 Chrome 扩展 |
| 隐私伪装 | 50+ 指纹伪装项、hosts 广告拦截、资源加密 |

## 项目由来

CodeToApp 基于 [WebToApp](https://github.com/shiaho777/web-to-app) 改造，做了以下调整：

- **包名** 改为 `com.codetoapp.app`，与上游区分开
- **应用名** 改为 CodeToApp，图标换成方块机器人 + 齿轮
- **移除第三方推广内容** —— 赞助卡片、作者项目列表、外链卡片
- **更新与版本历史** 指向 `benyeung08/code-to-app`，不再显示上游版本
- **新增 CodeToApp 应用类型** —— 通用源码项目，自动侦测语言与框架

## 下一步

- [快速上手](/guide/getting-started) —— 安装并做出第一个 APK
- [应用类型](/guide/app-types) —— 13 种类型各自适合什么场景
- [构建与导出](/guide/build-apk) —— 签名、对齐与发布
