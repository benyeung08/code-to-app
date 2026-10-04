package com.webtoapp.core.market

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CwsTagsTest {

    @Test
    fun `detects ad blocking from english name`() {
        val tags = CwsTags.fromName("AdBlock — block ads across the web")
        assertThat(tags).isNotEmpty()
        assertThat(tags.first().label).isEqualTo(com.webtoapp.core.i18n.Strings.cwsTagAdBlocking)
    }

    @Test
    fun `detects translate from chinese name`() {
        val tags = CwsTags.fromName("Google 翻译")
        assertThat(tags.any { it.label == com.webtoapp.core.i18n.Strings.cwsTagTranslate }).isTrue()
    }

    @Test
    fun `detects multiple tags when name matches several categories`() {
        val tags = CwsTags.fromName("Video Downloader for YouTube")
        val labels = tags.map { it.label }
        assertThat(labels).contains(com.webtoapp.core.i18n.Strings.cwsTagDownloader)
        assertThat(labels).contains(com.webtoapp.core.i18n.Strings.cwsTagVideoTool)
    }

    @Test
    fun `returns empty for unrelated name`() {
        val tags = CwsTags.fromName("Daily Weather Forecast")
        assertThat(tags).isEmpty()
    }

    @Test
    fun `caps at three tags`() {
        val tags = CwsTags.fromName("adblock translate dark mode download vpn")
        assertThat(tags.size).isAtMost(3)
    }
}
