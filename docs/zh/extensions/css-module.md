# CSS 插件

CSS 插件是纯样式覆盖——给站点做主题、重排版或暗色模式。它和 [HCJ 插件](/zh/extensions/js-module) 用同一个 `plugin.json` 清单，只是主体是样式表。

::: tip 推荐：`hcj.addStyle(css)`
页面样式通常直接写在 `main.js` 里，通过 `hcj.addStyle(css)` 注入——不需要独立文件，编辑器也只暴露 `main.js` / `panel.html`。单独的 `style.css` 文件为兼容保留（旧包或迁移包），见下文。
:::

## 文件结构

```
my-theme/
├── plugin.json    # 必需
├── main.js        # 必需 —— 可以是接近空的占位
├── style.css      # 兼容保留 —— 推荐改用 main.js 里的 hcj.addStyle
└── icon.png       # 可选
```

::: info `main.js` 仍然是必需的
即使是纯 CSS 插件也需要一个 `main.js`（最小占位即可）。把 `runAt` 设为 `document_start`，样式尽早生效，避免未样式化内容的闪烁。
:::

## `plugin.json`

```json
{
  "id": "dark-reader-lite",
  "name": "Dark Reader Lite",
  "description": "一个简单的暗色主题",
  "icon": "dark_mode",
  "runAt": "document_start",
  "matches": ["*://news.ycombinator.com/*"],
  "permissions": []
}
```

CSS 注入不需要权限——它就是包的一部分。

## CSS 如何注入

`style.css` 在 document-start 以 `<style id="hcj-css-<id>">` 元素注入，早于页面绘制和 `main.js` 运行。按插件、按文档幂等——反复导航不会重复叠加。

## `style.css` 示例

```css
:root {
  color-scheme: dark;
}
body {
  background: #111 !important;
  color: #ddd !important;
}
a {
  color: #60a5fa !important;
}
```

可用的暗色插件示例见 [`app/src/main/assets/plugins/`](https://github.com/shiaho777/web-to-app/tree/main/app/src/main/assets/plugins) 下的 `builtin-dark-mode`（样式由 `main.js` 注入）。
