package com.webtoapp.core.webview

/**
 * Classifies navigations and download URLs that need special handling around
 * Chromium's inability to render documents inline (issue #601: tapping a PDF
 * served by the embedded webserver navigated the main frame to an unrenderable
 * response — a black page — and never reached the download listener).
 *
 * Kept free of Android dependencies so it is unit-testable on the plain JVM.
 */
object DocumentDownloadPolicy {

    /** Document types Chromium cannot display inline; served without a
     *  Content-Disposition they navigate to a blank frame instead of downloading. */
    private val UNRENDERABLE_EXTENSIONS = setOf(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "zip", "rar", "7z", "epub", "apk"
    )

    private val MIME_BY_EXTENSION = mapOf(
        "pdf" to "application/pdf",
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "zip" to "application/zip",
        "rar" to "application/vnd.rar",
        "7z" to "application/x-7z-compressed",
        "epub" to "application/epub+zip",
        "apk" to "application/vnd.android.package-archive"
    )

    /**
     * True for http(s) main-frame targets the WebView would render as a blank or
     * garbled frame; those navigations should be handed to the download pipeline.
     */
    fun isUnrenderableDocumentUrl(url: String): Boolean {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        val ext = urlExtension(url) ?: return false
        return ext in UNRENDERABLE_EXTENSIONS
    }

    fun mimeTypeFor(url: String): String =
        urlExtension(url)?.let { MIME_BY_EXTENSION[it] } ?: "application/octet-stream"

    /** The app's own embedded runtimes bind these hosts; downloads from them are best
     *  fetched in-process instead of through the out-of-process system DownloadManager. */
    fun isLoopbackUrl(url: String): Boolean {
        val host = uriHost(url) ?: return false
        return host == "127.0.0.1" || host == "localhost" || host == "::1"
    }

    private fun urlExtension(url: String): String? {
        var segment = url.substringBefore('#').substringBefore('?')
        val slash = segment.lastIndexOf('/')
        if (slash >= 0) segment = segment.substring(slash + 1)
        val dot = segment.lastIndexOf('.')
        if (dot <= 0 || dot == segment.length - 1) return null
        val ext = segment.substring(dot + 1).lowercase()
        return ext.ifBlank { null }?.takeIf { it.length <= 5 }
    }

    private fun uriHost(url: String): String? {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd < 0) return null
        var rest = url.substring(schemeEnd + 3)
        rest = rest.substringBefore('/').substringBefore('?').substringBefore('#')
        rest = rest.substringAfterLast('@')
        if (rest.startsWith("[")) {
            return rest.substringBefore(']').removePrefix("[").lowercase()
        }
        return rest.substringBefore(':').lowercase()
    }
}
