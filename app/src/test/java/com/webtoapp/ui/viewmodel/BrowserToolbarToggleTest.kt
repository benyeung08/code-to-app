package com.webtoapp.ui.viewmodel

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test

class BrowserToolbarToggleTest {

    @Test
    fun `enabling the toolbar flips every item flag on`() {
        val config = WebViewConfig(toolbarShowFind = false)
        val result = config.withBrowserToolbarEnabled(true)

        assertThat(result.browserToolbarEnabled).isTrue()
        assertThat(result.toolbarShowTitle).isTrue()
        assertThat(result.toolbarShowUrl).isTrue()
        assertThat(result.toolbarShowBack).isTrue()
        assertThat(result.toolbarShowForward).isTrue()
        assertThat(result.toolbarShowRefresh).isTrue()
        assertThat(result.toolbarShowConsole).isTrue()
        assertThat(result.toolbarShowFind).isTrue()
    }

    @Test
    fun `disabling the toolbar flips every item flag off`() {
        val config = WebViewConfig(browserToolbarEnabled = true)
        val result = config.withBrowserToolbarEnabled(false)

        assertThat(result.browserToolbarEnabled).isFalse()
        assertThat(result.toolbarShowTitle).isFalse()
        assertThat(result.toolbarShowUrl).isFalse()
        assertThat(result.toolbarShowBack).isFalse()
        assertThat(result.toolbarShowForward).isFalse()
        assertThat(result.toolbarShowRefresh).isFalse()
        assertThat(result.toolbarShowConsole).isFalse()
        assertThat(result.toolbarShowFind).isFalse()
    }
}
