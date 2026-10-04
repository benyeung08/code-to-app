package com.webtoapp.core.market

import com.webtoapp.core.i18n.Strings

object CwsTags {

    data class FeatureTag(
        val label: String,
        val icon: String
    )

    private val RULES: List<Pair<List<String>, () -> FeatureTag>> = listOf(
        listOf("adblock", "ad block", "ad-block", "广告拦截", "广告过滤", "拦截广告") to
            { FeatureTag(Strings.cwsTagAdBlocking, "block") },
        listOf("translate", "翻译", "翻訳", "dict", "dictionary", "词典", "字典") to
            { FeatureTag(Strings.cwsTagTranslate, "translate") },
        listOf("dark", "night", "暗色", "夜间", "深色") to
            { FeatureTag(Strings.cwsTagDarkMode, "dark_mode") },
        listOf("download", "downloader", "下载", "video download", "video downloader") to
            { FeatureTag(Strings.cwsTagDownloader, "download") },
        listOf("screenshot", "截图", "full page", "capture", "录屏", "screen record") to
            { FeatureTag(Strings.cwsTagScreenshot, "screenshot_monitor") },
        listOf("password", "密码", "1password", "lastpass", "bitwarden", "vault") to
            { FeatureTag(Strings.cwsTagPassword, "password") },
        listOf("vpn", "proxy", "代理") to
            { FeatureTag(Strings.cwsTagVpn, "vpn_key") },
        listOf("note", "notion", "笔记", "memo", "memo") to
            { FeatureTag(Strings.cwsTagNotes, "edit_note") },
        listOf("tab", "session", "标签页", "标签管理") to
            { FeatureTag(Strings.cwsTagTabManager, "tab") },
        listOf("json", "developer", "regex", "webhook", "api", "开发者", "开发") to
            { FeatureTag(Strings.cwsTagDeveloper, "code") },
        listOf("color", "colour", "pick", "颜色", "取色") to
            { FeatureTag(Strings.cwsTagColorTool, "palette") },
        listOf("reading", "reader", "阅读", "simplify", "clean read") to
            { FeatureTag(Strings.cwsTagReading, "menu_book") },
        listOf("youtube", "bilibili", "video", "video speed", "sponsorblock") to
            { FeatureTag(Strings.cwsTagVideoTool, "smart_display") },
        listOf("privacy", "tracker", "隐私", "anti-track", "cookie") to
            { FeatureTag(Strings.cwsTagPrivacy, "shield") },
        listOf("grammar", "spelling", "拼写", "语法", "writing") to
            { FeatureTag(Strings.cwsTagWriting, "spellcheck") },
        listOf("stylish", "theme", "style", "css", "主题", "美化") to
            { FeatureTag(Strings.cwsTagStyling, "format_paint") },
        listOf("mail", "gmail", "邮件", "inbox") to
            { FeatureTag(Strings.cwsTagMail, "mail") },
        listOf("gesture", "mouse", "快捷", "shortcut", "vim") to
            { FeatureTag(Strings.cwsTagShortcuts, "touch_app") }
    )

    fun fromName(name: String): List<FeatureTag> {
        val lower = name.lowercase()
        val matches = mutableListOf<FeatureTag>()
        for ((keywords, factory) in RULES) {
            if (keywords.any { kw -> lower.contains(kw) }) {
                matches.add(factory())
            }
            if (matches.size >= 3) break
        }
        return matches
    }
}
