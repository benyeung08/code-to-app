---
layout: home

hero:
  name: CodeToApp
  text: 设备端 APK 工坊
  tagline: 远不止「套个网址」—— fork+exec 真实服务器运行时、加固的网络栈、导出可直接发布的包。全程不需要电脑。
  image:
    src: /logo.svg
    alt: CodeToApp
  actions:
    - theme: brand
      text: 快速上手
      link: /guide/getting-started
    - theme: alt
      text: 在 GitHub 查看
      link: https://github.com/benyeung08/code-to-app

features:
  - icon: ⚙️
    title: 真实运行时，不是模拟器
    details: Node.js、PHP、Python、Go 以原生二进制从应用存储里 fork+exec 起来 —— 像把 Termux 打包进一个可安装的 APK。
  - icon: 🛡️
    title: 加固的网络栈
    details: DNS-over-HTTPS、带本地 MITM 桥接的 TLS 指纹伪装、加密客户端问候（ECH）、按应用配置代理，以及为受限 SPA 提供的 CORS 绕过。
  - icon: 🔧
    title: 应用内完成二进制补丁
    details: 二进制 AXML/ARSC 改写、权限裁剪、V1/V2/V3 签名、Google Play 就绪的 AAB 导出 —— 全部在应用内通过 apksig 完成，没有远端构建队列。
  - icon: 🧩
    title: 插件化扩展
    details: 注入 JS/CSS 模块、Tampermonkey 式用户脚本，或 MV3 Chrome 扩展（可从 Chrome 应用商店实时搜索），无需重新构建宿主。
  - icon: 🎭
    title: 指纹伪装与隐私
    details: 50+ 项浏览器指纹伪装、带 20 个内置列表的 hosts 规则广告拦截、AES-256-GCM 资源加密，以及激活码门禁。
  - icon: 🌏
    title: 十种语言
    details: 中文、英文、阿拉伯文（RTL）、葡萄牙文、西班牙文、法文、德文、俄文、日文、韩文 —— 随时在设置里切换。
---

## 三步做出第一个 APK

<div class="cta-steps">

<div class="cta-step">
<span class="cta-step-num">1</span>
<h3>选一个类型</h3>
<p>网页、多站点、HTML、离线包、前端、PHP、WordPress、Node.js、Python、Go、媒体、相册，或者 <strong>CodeToApp</strong> —— 通用源码项目类型。</p>
</div>

<div class="cta-step">
<span class="cta-step-num">2</span>
<h3>填基本信息</h3>
<p>一个名称、一个 URL 或一个项目、一个图标，然后保存。所有类型共用同一套配置卡片：网络、隐私、外观与运行时。</p>
</div>

<div class="cta-step">
<span class="cta-step-num">3</span>
<h3>构建并分享</h3>
<p>导出 APK、分享给朋友，或继续微调后重新打包。</p>
</div>

</div>

## 应用类型

<div class="type-grid">

<div class="type-card">
<span class="type-icon">🌐</span>
<strong>网页 / 多站点</strong>
<p>网址套壳、标签式聚合门户与链接流。</p>
</div>

<div class="type-card">
<span class="type-icon">📦</span>
<strong>HTML / 离线包</strong>
<p>打包本地 HTML 或 zip 构建，也能把站点抓成一个自包含的离线 APK。</p>
</div>

<div class="type-card">
<span class="type-icon">⚛️</span>
<strong>前端</strong>
<p>把 React、Vue 或 Vite 构建产物做成 localhost 服务的 APK。</p>
</div>

<div class="type-card">
<span class="type-icon">🖥️</span>
<strong>Node.js / PHP / Python / Go</strong>
<p>fork+exec 原生二进制，在本地端口上提供服务。</p>
</div>

<div class="type-card">
<span class="type-icon">📝</span>
<strong>WordPress</strong>
<p>一个完整可移植站点，PHP + SQLite 直接在设备上跑起来。</p>
</div>

<div class="type-card">
<span class="type-icon">🖼️</span>
<strong>媒体 / 相册</strong>
<p>图片与视频播放器、相册、作品集，做成独立应用。</p>
</div>

<div class="type-card">
<span class="type-icon">🤖</span>
<strong>Agent</strong>
<p>带工具调用的助手，最多 57 个内置工具，可以构建、编辑并操作整个应用。</p>
</div>

<div class="type-card type-card-brand">
<span class="type-icon">🤖</span>
<strong>CodeToApp</strong>
<p>通用源码项目：导入一个源码目录后自动侦测语言与框架，再委派给对应的既有运行时启动器。</p>
</div>

</div>

## 还有更多

- **插件模块** —— 注入 JS/CSS、用户脚本或 MV3 Chrome 扩展到任意生成的应用里
- **广告拦截** —— 20 个内置过滤列表加按应用订阅，编译进最终 APK
- **Linux 环境** —— Termux 风格的真实工具链，用来构建和运行项目
- **本地服务器** —— 为每种本地运行时提供冲突策略、真正的停止处理器与 DNS 桥接
- **克隆与改名** —— 克隆并重新品牌化已安装的 APK、批量导入定义、导出模板

构建器自己做二进制补丁 —— AXML/ARSC 改写、权限裁剪、AES-256-GCM 资源加密、16 KB 页对齐的原生库 —— 并保持一个低 targetSdk 的外壳，让 fork+exec 运行时继续可用。开发者文档覆盖了完整的导出流水线。

<div class="cta-final">

## 准备好构建你的第一个 APK 了吗？

[下载最新 APK](https://github.com/benyeung08/code-to-app/raw/apk/builds/latest.apk){.cta-button} · [查看源码](https://github.com/benyeung08/code-to-app)

</div>
