package com.webtoapp.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExtensionModuleTest {

    @Test
    fun `matchesUrl returns true when no rules configured`() {
        val module = ExtensionModule(
            name = "No Rules Module",
            code = "console.log('ok')"
        )

        assertThat(module.matchesUrl("https://example.com")).isTrue()
    }

    @Test
    fun `matchesUrl applies include and exclude rules`() {
        val module = ExtensionModule(
            name = "Url Rule Module",
            code = "console.log('ok')",
            urlMatches = listOf(
                UrlMatchRule(pattern = "*example.com*"),
                UrlMatchRule(pattern = "https://admin.example.com/*", exclude = true)
            )
        )

        assertThat(module.matchesUrl("https://shop.example.com/product")).isTrue()
        assertThat(module.matchesUrl("https://admin.example.com/panel")).isFalse()
        assertThat(module.matchesUrl("https://other.com")).isFalse()
    }

    @Test
    fun `invalid regex rule fails closed without crashing`() {
        val module = ExtensionModule(
            name = "Regex Module",
            code = "console.log('ok')",
            urlMatches = listOf(
                UrlMatchRule(pattern = "[invalid-regex", isRegex = true)
            )
        )

        assertThat(module.matchesUrl("https://example.com")).isFalse()
    }

    @Test
    fun `sanitized coerces Gson-null fields back to defaults`() {
        // Gson allocates via Unsafe and leaves Kotlin non-null fields null when the JSON
        // omits them (Kotlin defaults are bypassed). sanitized() must restore the declared
        // defaults so consumers never observe null in a non-null field (regression: the
        // ModuleCard NullPointerException when rendering such a module).
        val module = com.google.gson.Gson().fromJson("{}", ExtensionModule::class.java)

        // Precondition: plain Gson left the non-null object fields as null (the bug condition).
        assertThat(module.name as String?).isNull()
        assertThat(module.description as String?).isNull()
        assertThat(module.storeIconPath as String?).isNull()

        val sanitized = module.sanitized()

        assertThat(sanitized.id).isNotEmpty()
        assertThat(sanitized.name).isEqualTo("")
        assertThat(sanitized.description).isEqualTo("")
        assertThat(sanitized.icon).isEqualTo("package")
        assertThat(sanitized.storeIconPath).isEqualTo("")
        assertThat(sanitized.world).isEqualTo("ISOLATED")
        assertThat(sanitized.category).isEqualTo(ModuleCategory.OTHER)
        assertThat(sanitized.tags).isEmpty()
        assertThat(sanitized.storeTags).isEmpty()
        assertThat(sanitized.version).isNotNull()
    }
}
