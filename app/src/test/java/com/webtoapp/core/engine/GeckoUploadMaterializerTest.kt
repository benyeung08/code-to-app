package com.webtoapp.core.engine

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayInputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
class GeckoUploadMaterializerTest {

    private lateinit var context: Context
    private lateinit var uploadsDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        uploadsDir = File(context.cacheDir, "gecko_uploads_test").apply { mkdirs() }
    }

    private fun registerUpload(uri: Uri, bytes: ByteArray) {
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(bytes))
    }

    private fun contentUri(id: Int): Uri = Uri.parse("content://media/external/images/media/$id")

    @Test
    fun `content uri is materialized into a file uri carrying the picked bytes`() {
        val uri = contentUri(1)
        registerUpload(uri, "hello upload".toByteArray())

        val prepared = GeckoUploadMaterializer.prepare(
            arrayOf(uri), uploadsDir, context.contentResolver
        )

        assertEquals(1, prepared.size)
        assertEquals("file", prepared[0].scheme)
        val copy = File(prepared[0].path!!)
        assertTrue(copy.isFile)
        // No display name resolves under the bare shadow resolver, so the fallback name applies.
        assertTrue(copy.name.startsWith("upload"))
        assertEquals("hello upload", copy.readText())
    }

    @Test
    fun `unreadable content uri falls back to the original uri`() {
        val uri = contentUri(2)

        val prepared = GeckoUploadMaterializer.prepare(
            arrayOf(uri), uploadsDir, context.contentResolver
        )

        assertEquals(listOf(uri), prepared.toList())
    }

    @Test
    fun `non-content schemes pass through untouched`() {
        val fileUri = Uri.fromFile(File.createTempFile("direct", ".txt"))
        val mailtoUri = Uri.parse("mailto:someone@example.com")

        val prepared = GeckoUploadMaterializer.prepare(arrayOf(fileUri, mailtoUri), uploadsDir, null)

        assertEquals(listOf(fileUri, mailtoUri), prepared.toList())
    }

    @Test
    fun `same-named copies are deduplicated without clobbering each other`() {
        val first = contentUri(4)
        val second = contentUri(5)
        registerUpload(first, "first".toByteArray())
        registerUpload(second, "second".toByteArray())

        val prepared = GeckoUploadMaterializer.prepare(
            arrayOf(first, second), uploadsDir, context.contentResolver
        )

        assertFalse(prepared[0].path == prepared[1].path)
        assertEquals("first", File(prepared[0].path!!).readText())
        assertEquals("second", File(prepared[1].path!!).readText())
    }

    @Test
    fun `stale uploads are purged but fresh ones kept`() {
        val now = System.currentTimeMillis()
        val stale = File(uploadsDir, "stale.jpg")
            .apply { writeText("old"); setLastModified(now - GeckoUploadMaterializer.UPLOAD_TTL_MS - 60_000L) }
        val fresh = File(uploadsDir, "fresh.jpg").apply { writeText("new") }

        GeckoUploadMaterializer.purgeStale(uploadsDir, now)

        assertFalse(stale.exists())
        assertTrue(fresh.exists())
    }

    @Test
    fun `sanitize strips separators, blanks and dot-only names`() {
        assertEquals("upload", GeckoUploadMaterializer.sanitizeUploadFileName(null))
        assertEquals("upload", GeckoUploadMaterializer.sanitizeUploadFileName("   "))
        assertEquals("upload", GeckoUploadMaterializer.sanitizeUploadFileName("..."))
        val nasty = GeckoUploadMaterializer.sanitizeUploadFileName("../../etc/passwd")
        assertFalse(nasty.contains('/'))
        assertEquals("report 2026_.pdf", GeckoUploadMaterializer.sanitizeUploadFileName("report 2026?.pdf"))
    }

    @Test
    fun `over-long names keep their extension`() {
        val longName = "${"a".repeat(150)}.pdf"
        val sanitized = GeckoUploadMaterializer.sanitizeUploadFileName(longName)

        assertTrue(sanitized.length <= 100)
        assertTrue(sanitized.endsWith(".pdf"))
    }
}
