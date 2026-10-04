package com.webtoapp.core.webview

/**
 * Hosts that carry cross-site authentication / challenge flows.
 *
 * Sign-in must complete inside the app's own WebView: the site session lives in this
 * WebView's cookie jar, so routing an OAuth navigation to an external browser can never
 * hand the session back (#601 — "sign-in never completes"). Embedded provider frames
 * additionally need third-party cookies, which Android WebView disables by default.
 *
 * Kept free of Android dependencies so it is unit-testable on the plain JVM.
 */
object OAuthProviderHosts {

    val HOST_SUFFIXES = setOf(
        "accounts.google.com",
        "accounts.youtube.com",
        "myaccount.google.com",

        "www.facebook.com",
        "m.facebook.com",

        "appleid.apple.com",

        "login.microsoftonline.com",
        "login.live.com",

        "github.com",

        "api.twitter.com",
        "api.x.com"
    )

    fun isOAuthHost(url: String): Boolean {
        val host = extractHost(url) ?: return false
        return HOST_SUFFIXES.any { suffix ->
            host == suffix || host.endsWith(".$suffix")
        }
    }

    fun extractHost(url: String): String? {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd <= 0) return null
        var rest = url.substring(schemeEnd + 3)
        rest = rest.substringBefore('/').substringBefore('?').substringBefore('#')
        rest = rest.substringAfterLast('@')
        return rest.substringBefore(':').lowercase().ifEmpty { null }
    }
}
