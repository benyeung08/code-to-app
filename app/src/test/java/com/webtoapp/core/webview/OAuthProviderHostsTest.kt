package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OAuthProviderHostsTest {

    @Test
    fun `oauth provider hosts are detected`() {
        assertThat(OAuthProviderHosts.isOAuthHost("https://accounts.google.com/o/oauth2/auth?client_id=x")).isTrue()
        assertThat(OAuthProviderHosts.isOAuthHost("https://accounts.google.com/")).isTrue()
        assertThat(OAuthProviderHosts.isOAuthHost("https://github.com/login")).isTrue()
        assertThat(OAuthProviderHosts.isOAuthHost("https://appleid.apple.com/auth")).isTrue()
        assertThat(OAuthProviderHosts.isOAuthHost("https://login.microsoftonline.com/common/oauth2")).isTrue()
        assertThat(OAuthProviderHosts.isOAuthHost("https://api.x.com/oauth")).isTrue()
    }

    @Test
    fun `provider subdomains are detected`() {
        assertThat(OAuthProviderHosts.isOAuthHost("https://www.accounts.google.com/")).isTrue()
        assertThat(OAuthProviderHosts.isOAuthHost("https://sub.github.com/")).isTrue()
    }

    @Test
    fun `lookalike hosts are not detected`() {
        assertThat(OAuthProviderHosts.isOAuthHost("https://evil-github.com/login")).isFalse()
        assertThat(OAuthProviderHosts.isOAuthHost("https://github.com.evil.io/x")).isFalse()
        assertThat(OAuthProviderHosts.isOAuthHost("https://example.com/?next=https://accounts.google.com/")).isFalse()
    }

    @Test
    fun `non http urls and garbage are not detected`() {
        assertThat(OAuthProviderHosts.isOAuthHost("file:///x")).isFalse()
        assertThat(OAuthProviderHosts.isOAuthHost("about:blank")).isFalse()
        assertThat(OAuthProviderHosts.isOAuthHost("not a url")).isFalse()
        assertThat(OAuthProviderHosts.isOAuthHost("")).isFalse()
    }
}
