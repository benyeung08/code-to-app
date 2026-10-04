package com.webtoapp.core.agent.session

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression tests for issue #712: restoring a backup exported from a
 * pre-Agent (AI Coding era) release writes the legacy `aicoding_sessions_v1`
 * blob back onto the device. Those sessions predate the composer attachments
 * (#374), the context picker (#381) and built-APK persistence (#447), so their
 * JSON lacks `userAttachments`, `contextAppIds`, `contextModuleIds` and
 * `builtApks`. Gson bypasses Kotlin constructor defaults, so those non-null
 * properties deserialise as null and every direct use crashed (opening Agent
 * rendered history via `message.userAttachments.isNotEmpty()` → NPE).
 *
 * The fixture `legacy_agent_sessions_v1.json` is a sanitised copy of a real
 * user backup: identical field structure, truncated/synthetic content.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionStoreLegacyJsonTest {

    private lateinit var store: SessionStore
    private lateinit var files: ProjectFileManager

    @Before
    fun setUp() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        files = ProjectFileManager(ctx)
        store = SessionStore(ctx, files)
    }

    private fun legacyJson(): String =
        javaClass.getResourceAsStream("/legacy_agent_sessions_v1.json")!!
            .readBytes().decodeToString()

    @Test
    fun `decode of legacy session JSON does not crash and normalises missing lists`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())

        assertThat(sessions).hasSize(5)
        sessions.forEach { session ->
            // Previously-unguarded fields must never be null after decode.
            session.config.builtApks
            session.config.contextAppIds
            session.config.contextModuleIds
            session.config.customRules
            session.messages.forEach { message ->
                message.userAttachments
                message.thinkingSegments
                message.toolCalls
                message.producedFiles
                message.attachments
                message.mentionedFiles
            }
        }
    }

    @Test
    fun `legacy messages expose empty userAttachments via safe accessor`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())
        val messages = sessions.flatMap { it.messages }
        // The fixture mirrors the real legacy schema: no userAttachments key anywhere.
        assertThat(messages).isNotEmpty()
        messages.forEach { message ->
            assertThat(message.userAttachmentsSafe).isEmpty()
            assertThat(message.thinkingSegmentsSafe).isEmpty()
        }
    }

    @Test
    fun `legacy config exposes empty context and built-apk lists via safe accessors`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())
        sessions.forEach { session ->
            assertThat(session.config.contextAppIdsSafe).isEmpty()
            assertThat(session.config.contextModuleIdsSafe).isEmpty()
            assertThat(session.config.builtApksSafe).isEmpty()
        }
    }

    @Test
    fun `decoded legacy blob re-serialises with normalised lists and decodes again`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())
        // SessionStore.persist always writes via gson.toJson; the normalised list must
        // round-trip so the first write after restore heals the stored blob.
        val raw = GsonProvider.gson.toJson(sessions)
        val again = store.decodeForTest(raw)
        assertThat(again).hasSize(sessions.size)
        again.flatMap { it.messages }.forEach { message ->
            assertThat(message.userAttachmentsSafe).isEmpty()
        }
    }
}
