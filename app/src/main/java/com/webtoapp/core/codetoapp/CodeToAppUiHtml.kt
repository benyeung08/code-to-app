package com.webtoapp.core.codetoapp

import com.webtoapp.core.codetoapp.CodeToAppUiDesigner.UiComponentType
import com.webtoapp.core.codetoapp.CodeToAppUiDesigner.UiNode

/**
 * 把设计器的节点树渲染成静态 HTML + CSS。
 *
 * 用 inline style 而不是 class：节点可以无限嵌套且 id 由使用者产生，
 * inline 能完全避开样式冲突，也让「改一个属性立刻看到结果」这件事变得很直接。
 *
 * 产生的档案会写回专案目录，因此可以直接用既有的本端 HTTP 服务打开，
 * 也能被 APK 打包流程一起带走。
 */
object CodeToAppUiHtml {

    fun render(root: UiNode, title: String = "CodeToApp UI"): String {
        val body = renderNode(root, 0)
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<title>${escapeHtml(title)}</title>
<style>
  * { box-sizing: border-box; }
  html, body {
    margin: 0; padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    background: #F5F5F5;
  }
  .cta-root { min-height: 100vh; }
  button, input { font-family: inherit; }
</style>
</head>
<body>
$body
</body>
</html>
""".trimIndent()
    }

    private fun renderNode(node: UiNode, depth: Int): String {
        val indent = "  ".repeat(depth + 1)
        val style = buildStyle(node)

        return when (node.type) {
            UiComponentType.COLUMN, UiComponentType.ROW, UiComponentType.CARD -> {
                val dir = if (node.type == UiComponentType.ROW) "row" else "column"
                val children = node.children.joinToString("\n") { renderNode(it, depth + 1) }
                val cls = if (depth == 0) "cta-root " else ""
                "$indent<div${cls}style=\"$style\">\n$children\n$indent</div>"
            }

            UiComponentType.TEXT -> {
                val text = node.props["text"].orEmpty()
                "$indent<p style=\"$style\">${escapeHtml(text)}</p>"
            }

            UiComponentType.BUTTON -> {
                val text = node.props["text"].orEmpty()
                "$indent<button type=\"button\" style=\"$style\" onclick=\"console.log('clicked: ${escapeJs(text)}')\">${escapeHtml(text)}</button>"
            }

            UiComponentType.INPUT -> {
                val hint = node.props["hint"].orEmpty()
                "$indent<input type=\"text\" placeholder=\"${escapeHtml(hint)}\" style=\"$style\" />"
            }

            UiComponentType.IMAGE -> {
                val src = node.props["src"].orEmpty()
                if (src.isBlank()) {
                    "$indent<div style=\"$style;background:#E0E0E0;display:flex;align-items:center;justify-content:center;color:#9E9E9E;font-size:13px\">image</div>"
                } else {
                    "$indent<img src=\"${escapeHtml(src)}\" alt=\"\" style=\"$style;object-fit:cover;width:100%\" />"
                }
            }

            UiComponentType.CHECKBOX -> {
                val text = node.props["text"].orEmpty()
                val checked = node.props["checked"] == "true"
                val flag = if (checked) " checked" else ""
                "$indent<label style=\"display:flex;align-items:center;gap:8px;$style\">" +
                    "<input type=\"checkbox\"$flag />" +
                    "<span>${escapeHtml(text)}</span></label>"
            }

            UiComponentType.SWITCH -> {
                val text = node.props["text"].orEmpty()
                val checked = node.props["checked"] == "true"
                val flag = if (checked) " checked" else ""
                "$indent<label style=\"display:flex;align-items:center;gap:8px;$style\">" +
                    "<input type=\"checkbox\"$flag style=\"width:18px;height:18px;accent-color:${cssColor(node.props["color"])}\" />" +
                    "<span>${escapeHtml(text)}</span></label>"
            }

            UiComponentType.DIVIDER -> {
                "$indent<hr style=\"$style;border:none;\" />"
            }

            UiComponentType.SPACER -> {
                "$indent<div style=\"$style\"></div>"
            }
        }
    }

    // ---------------------------------------------------------------- 样式

    private fun buildStyle(node: UiNode): String {
        val p = node.props
        val parts = mutableListOf<String>()

        fun num(key: String, suffix: String = "px") {
            p[key]?.toIntOrNull()?.let { parts += "$key:$it$suffix" }
        }

        when (node.type) {
            UiComponentType.COLUMN, UiComponentType.ROW, UiComponentType.CARD -> {
                parts += "display:flex"
                parts += "flex-direction:" + if (node.type == UiComponentType.ROW) "row" else "column"
                parts += if (node.type == UiComponentType.ROW) "align-items:center" else "align-items:stretch"
                num("padding")
                num("gap")
                val radius = p["radius"]?.toIntOrNull() ?: 0
                if (radius > 0) parts += "border-radius:${radius}px"
                val bg = p["background"]
                if (!bg.isNullOrBlank() && bg != "#00000000") {
                    parts += "background:${cssColor(bg)}"
                    if (node.type == UiComponentType.CARD) {
                        parts += "box-shadow:0 1px 3px rgba(0,0,0,.12)"
                    }
                }
            }

            UiComponentType.TEXT -> {
                parts += "margin:0"
                num("size", "px").also { parts += "font-size:${p["size"] ?: "16"}px" }
                parts += "color:${cssColor(p["color"])}"
                parts += "font-weight:${p["weight"] ?: "normal"}"
                parts += "text-align:${when (p["align"]) { "center" -> "center"; "end" -> "right"; else -> "left" }}"
            }

            UiComponentType.BUTTON -> {
                parts += "font-size:${p["size"] ?: "15"}px"
                parts += "color:${cssColor(p["color"])}"
                parts += "background:${cssColor(p["background"])}"
                parts += "border:none"
                parts += "padding:12px 16px"
                parts += "cursor:pointer"
                parts += "width:100%"
                val radius = p["radius"]?.toIntOrNull() ?: 8
                parts += "border-radius:${radius}px"
            }

            UiComponentType.INPUT -> {
                parts += "font-size:${p["size"] ?: "15"}px"
                parts += "color:${cssColor(p["color"])}"
                parts += "padding:12px"
                parts += "border:1px solid #BDBDBD"
                parts += "background:#FFFFFF"
                parts += "width:100%"
                parts += "outline:none"
                val radius = p["radius"]?.toIntOrNull() ?: 8
                parts += "border-radius:${radius}px"
            }

            UiComponentType.IMAGE -> {
                num("height")
                val radius = p["radius"]?.toIntOrNull() ?: 0
                if (radius > 0) parts += "border-radius:${radius}px"
            }

            UiComponentType.CHECKBOX, UiComponentType.SWITCH -> {
                parts += "color:${cssColor(p["color"])}"
                parts += "font-size:15px"
            }

            UiComponentType.DIVIDER -> {
                parts += "background:${cssColor(p["color"])}"
                num("height")
                parts += "width:100%"
            }

            UiComponentType.SPACER -> {
                num("height")
                parts += "width:100%"
            }
        }

        return parts.joinToString(";")
    }

    /**
     * 设计器内部统一存 ARGB（#AARRGGBB），CSS 只认 #RRGGBB / rgba()。
     * 透明（alpha = 00）直接不加背景，让父层透出来。
     */
    private fun cssColor(raw: String?): String {
        if (raw.isNullOrBlank()) return "transparent"
        val hex = raw.trim().removePrefix("#")
        return when (hex.length) {
            8 -> {
                val aa = hex.substring(0, 2)
                val rr = hex.substring(2, 4)
                val gg = hex.substring(4, 6)
                val bb = hex.substring(6, 8)
                val alpha = aa.toIntOrNull(16) ?: 255
                if (alpha == 255) "#$rr$gg$bb" else "rgba(${rr.toInt(16)},${gg.toInt(16)},${bb.toInt(16)},${alpha / 255f})"
            }
            6 -> "#$hex"
            3 -> "#" + hex.map { "$it$it" }.joinToString("")
            else -> "transparent"
        }
    }

    // ---------------------------------------------------------------- 逸出

    private fun escapeHtml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun escapeJs(s: String): String = s
        .replace("\\", "\\\\")
        .replace("'", "\\'")

    // ---------------------------------------------------------------- 输出

    /** 写入专案目录；档名固定，方便被打包流程带走。 */
    fun writePreview(projectDir: java.io.File, root: UiNode, title: String): java.io.File? {
        if (!projectDir.exists()) projectDir.mkdirs()
        val file = java.io.File(projectDir, "ui-preview.html")
        return runCatching {
            file.writeText(render(root, title))
            file
        }.getOrNull()
    }
}
