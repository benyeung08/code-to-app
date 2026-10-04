package com.webtoapp.core.i18n

object PreviewHtmlSupport {

    fun escapeText(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun htmlLang(): String = when (Strings.currentLanguage.value) {
        AppLanguage.CHINESE -> "zh"
        AppLanguage.ENGLISH -> "en"
        AppLanguage.ARABIC -> "ar"
        AppLanguage.PORTUGUESE -> "pt"
        AppLanguage.SPANISH -> "es"
        AppLanguage.FRENCH -> "fr"
        AppLanguage.GERMAN -> "de"
        AppLanguage.RUSSIAN -> "ru"
        AppLanguage.JAPANESE -> "ja"
        AppLanguage.KOREAN -> "ko"
    }
}
