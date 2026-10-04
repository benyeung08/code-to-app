package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import com.webtoapp.util.GsonProvider
import org.junit.Test

class PluginManifestTest {

    @Test
    fun `minimal manifest parses with defaults and does not throw in matchRules`() {
        // Hand-authored plugin.json files routinely omit optional collections.
        // Gson's Unsafe path leaves absent fields JVM-null despite non-null
        // Kotlin types, which used to NPE inside matchRules and kill the whole
        // built-in plugin load. The hand parser must apply defaults instead.
        val manifest = PluginManifest.fromJson("""{"name":"No Optionals"}""")

        assertThat(manifest).isNotNull()
        manifest!!
        assertThat(manifest.name).isEqualTo("No Optionals")
        assertThat(manifest.matches).containsExactly("*")
        assertThat(manifest.excludeMatches).isEmpty()
        assertThat(manifest.permissions).isEmpty()
        assertThat(manifest.gmGrants).isEmpty()
        assertThat(manifest.requireUrls).isEmpty()
        assertThat(manifest.resources).isEmpty()
        assertThat(manifest.toolbar).isTrue()
        assertThat(manifest.matchRules()).hasSize(1)
        assertThat(manifest.resolvedPermissions()).isEmpty()
        assertThat(manifest.resolvedRunAt()).isEqualTo(PluginRunAt.DOCUMENT_END)
    }

    @Test
    fun `full manifest parses every field`() {
        val json = """
            {
              "id": "p1", "name": "Full", "version": "2.0.0",
              "description": "d", "author": "a", "homepage": "h",
              "icon": "dark_mode",
              "matches": ["*://*.a.com/*", "/^https:\\/\\/b\\./"],
              "excludeMatches": ["*://x.a.com/*"],
              "runAt": "document_start",
              "permissions": ["STORAGE", "FETCH"],
              "toolbar": false,
              "preferredEntry": "FLOATING_HANDLE",
              "gmGrants": ["GM_setValue"],
              "requireUrls": ["https://cdn.example.com/lib.js"],
              "resources": {"logo": "https://cdn.example.com/logo.png"},
              "noframes": true,
              "legacyCompat": true
            }
        """.trimIndent()

        val m = PluginManifest.fromJson(json)!!
        assertThat(m.id).isEqualTo("p1")
        assertThat(m.version).isEqualTo("2.0.0")
        assertThat(m.matches).hasSize(2)
        assertThat(m.excludeMatches).containsExactly("*://x.a.com/*")
        assertThat(m.runAt).isEqualTo("document_start")
        assertThat(m.toolbar).isFalse()
        assertThat(m.preferredEntry).isEqualTo("FLOATING_HANDLE")
        assertThat(m.gmGrants).containsExactly("GM_setValue")
        assertThat(m.requireUrls).containsExactly("https://cdn.example.com/lib.js")
        assertThat(m.resources).containsEntry("logo", "https://cdn.example.com/logo.png")
        assertThat(m.noframes).isTrue()
        assertThat(m.legacyCompat).isTrue()
        assertThat(m.resolvedRunAt()).isEqualTo(PluginRunAt.DOCUMENT_START)
        assertThat(m.resolvedPermissions())
            .containsExactly(PluginPermission.STORAGE, PluginPermission.FETCH)
    }

    @Test
    fun `regex rules are recognized and glob rules are not`() {
        val m = PluginManifest.fromJson(
            """{"name":"R","matches":["/re/","*://a.com/*"]}"""
        )!!
        val rules = m.matchRules()
        assertThat(rules[0].isRegex).isTrue()
        assertThat(rules[0].pattern).isEqualTo("re")
        assertThat(rules[1].isRegex).isFalse()
        assertThat(rules[1].pattern).isEqualTo("*://a.com/*")
    }

    @Test
    fun `blank name yields null manifest`() {
        assertThat(PluginManifest.fromJson("""{"id":"x"}""")).isNull()
        assertThat(PluginManifest.fromJson("not json")).isNull()
        assertThat(PluginManifest.fromJson("[1,2]")).isNull()
    }

    @Test
    fun `manifest to json round trips through gson`() {
        val m = PluginManifest(name = "RT", matches = listOf("*"), permissions = listOf("STORAGE"))
        val back = PluginManifest.fromJson(GsonProvider.gson.toJson(m))
        assertThat(back).isNotNull()
        assertThat(back!!.name).isEqualTo("RT")
        assertThat(back.permissions).containsExactly("STORAGE")
    }
}
