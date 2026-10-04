package com.webtoapp.core.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Providers removed from the catalog deserialize to a null enum under Gson; the
 * config manager must migrate those keys onto CUSTOM while preserving the user's
 * endpoint and key setup.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AiConfigManagerMigrationTest {

    private lateinit var context: Context
    private lateinit var manager: AiConfigManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = AiConfigManager(context)
    }

    @Test
    fun `removed provider migrates to custom with its legacy base url`() {
        val json = """
            [{
                "id": "key-1",
                "provider": "GROQ",
                "apiKey": "gsk_test",
                "createdAt": 1700000000000
            }]
        """.trimIndent()

        val configs = manager.parseApiKeyConfigs(json)!!

        assertThat(configs).hasSize(1)
        val migrated = configs.first()
        assertThat(migrated.provider).isEqualTo(AiProvider.CUSTOM)
        assertThat(migrated.baseUrl).isEqualTo("https://api.groq.com/openai")
        assertThat(migrated.apiKey).isEqualTo("gsk_test")
    }

    @Test
    fun `removed provider keeps an explicit base url override`() {
        val json = """
            [{
                "id": "key-2",
                "provider": "SILICONFLOW",
                "apiKey": "sk_test",
                "baseUrl": "https://my-proxy.example.com",
                "apiFormat": "OPENAI_COMPATIBLE",
                "createdAt": 1700000000000
            }]
        """.trimIndent()

        val configs = manager.parseApiKeyConfigs(json)!!

        val migrated = configs.first()
        assertThat(migrated.provider).isEqualTo(AiProvider.CUSTOM)
        assertThat(migrated.baseUrl).isEqualTo("https://my-proxy.example.com")
        assertThat(migrated.apiFormat).isEqualTo(ApiFormat.OPENAI_COMPATIBLE)
    }

    @Test
    fun `unknown provider names also migrate to custom`() {
        val json = """
            [{
                "id": "key-3",
                "provider": "SOME_FUTURE_PROVIDER",
                "apiKey": "sk_test",
                "baseUrl": "https://future.example.com",
                "createdAt": 1700000000000
            }]
        """.trimIndent()

        val configs = manager.parseApiKeyConfigs(json)!!

        assertThat(configs.first().provider).isEqualTo(AiProvider.CUSTOM)
        assertThat(configs.first().baseUrl).isEqualTo("https://future.example.com")
    }

    @Test
    fun `saved models referencing removed providers migrate to custom`() {
        val json = """
            [{
                "id": "model-1",
                "model": {
                    "id": "llama-3-70b",
                    "name": "Llama 3 70B",
                    "provider": "GROQ",
                    "capabilities": ["TEXT"],
                    "contextLength": 8192
                },
                "apiKeyId": "key-1",
                "capabilities": ["TEXT"]
            }]
        """.trimIndent()

        val saved = manager.parseSavedModels(json)!!

        assertThat(saved).hasSize(1)
        assertThat(saved.first().model.provider).isEqualTo(AiProvider.CUSTOM)
        assertThat(saved.first().model.isCustom).isTrue()
        assertThat(saved.first().model.id).isEqualTo("llama-3-70b")
    }

    @Test
    fun `legacy configs missing apiFormat are normalized`() {
        val json = """
            [{
                "id": "key-5",
                "provider": "DEEPSEEK",
                "apiKey": "sk_live",
                "createdAt": 1700000000000
            }]
        """.trimIndent()

        val configs = manager.parseApiKeyConfigs(json)!!

        assertThat(configs.first().apiFormat).isEqualTo(ApiFormat.OPENAI_COMPATIBLE)
    }

    @Test
    fun `live providers are not migrated`() {
        val json = """
            [{
                "id": "key-4",
                "provider": "DEEPSEEK",
                "apiKey": "sk_live",
                "createdAt": 1700000000000
            }]
        """.trimIndent()

        val configs = manager.parseApiKeyConfigs(json)!!

        assertThat(configs.first().provider).isEqualTo(AiProvider.DEEPSEEK)
        assertThat(configs.first().baseUrl).isNull()
    }
}
