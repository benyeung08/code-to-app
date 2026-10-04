package com.webtoapp.core.engine

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Turns file-picker results into something GeckoView's FilePrompt.confirm() can hand to the
 * page (#638). confirm() never reads streams — it resolves each Uri to a local path string via
 * the `_data` column / externalstorage document ids, and silently feeds null paths into the
 * upload when a provider refuses (galleries, cloud, most vendor pickers). Firefox avoids this
 * by copying every content:// through ContentResolver into a cache-backed file first
 * (android-components toFileUri); this mirrors that for the shell bridge.
 *
 * Copy failures fall back to the original Uri so one unreadable item cannot sink the whole
 * selection — upstream resolution may still succeed where the copy could not.
 *
 * Copies must outlive the pick dialog (pages upload asynchronously) but cannot be kept forever:
 * stale files are purged on the next prompt and everything is wiped when the engine is
 * destroyed. The caller runs I/O off the UI thread; FilePrompt.confirm() is @UiThread.
 */
internal object GeckoUploadMaterializer {

    private const val TAG = "GeckoUploadMaterializer"

    const val UPLOAD_TTL_MS: Long = 60L * 60L * 1000L

    private const val MAX_FILE_NAME_LENGTH = 100

    /**
     * Returns Uris safe to pass to FilePrompt.confirm(): content:// entries are copied into
     * [uploadsDir] and replaced by file:// Uris pointing at the copies, other schemes pass
     * through unchanged. Never throws.
     */
    fun prepare(
        uris: Array<Uri>,
        uploadsDir: File,
        resolver: ContentResolver?,
        nowMs: Long = System.currentTimeMillis()
    ): Array<Uri> {
        runCatching { purgeStale(uploadsDir, nowMs) }
            .onFailure { AppLogger.w(TAG, "Upload cache purge failed: ${it.message}") }
        return Array(uris.size) { i -> materialize(uris[i], uploadsDir, resolver) }
    }

    /** Deletes every cached upload (engine teardown: all sessions are gone by then). */
    fun purgeAll(uploadsDir: File) {
        val files = uploadsDir.listFiles() ?: return
        for (file in files) {
            runCatching { file.delete() }
        }
    }

    internal fun materialize(uri: Uri, uploadsDir: File, resolver: ContentResolver?): Uri {
        return when (uri.scheme) {
            "content" -> runCatching { copyToCache(uri, uploadsDir, resolver) }
                .onFailure { failure ->
                    AppLogger.w(TAG, "Content URI not materialized, using as-is (${failure.javaClass.simpleName}): $uri")
                }
                .getOrNull()?.let { Uri.fromFile(it) } ?: uri
            else -> uri
        }
    }

    internal fun purgeStale(uploadsDir: File, nowMs: Long) {
        val files = uploadsDir.listFiles() ?: return
        for (file in files) {
            if (!file.isFile) continue
            // A future lastModified (clock skew) yields a negative age and is kept.
            if (nowMs - file.lastModified() > UPLOAD_TTL_MS) {
                runCatching { file.delete() }
            }
        }
    }

    private fun copyToCache(uri: Uri, uploadsDir: File, resolver: ContentResolver?): File {
        if (!uploadsDir.exists()) uploadsDir.mkdirs()
        if (resolver == null) throw IllegalStateException("No ContentResolver to read $uri")

        val target = uniqueTarget(uploadsDir, sanitizeUploadFileName(displayName(resolver, uri)))
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(target).use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Provider returned no stream for $uri")
        return target
    }

    private fun displayName(resolver: ContentResolver, uri: Uri): String? {
        return try {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> displayNameFrom(cursor) }
        } catch (e: Exception) {
            null
        }
    }

    private fun displayNameFrom(cursor: Cursor): String? {
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index < 0 || !cursor.moveToFirst()) return null
        return try { cursor.getString(index) } catch (e: Exception) { null }
    }

    internal fun sanitizeUploadFileName(raw: String?): String {
        var name = raw?.map { ch ->
            if (ch.isLetterOrDigit()) ch else if (ch == '.' || ch == '-' || ch == '_' || ch == ' ') ch else '_'
        }?.joinToString("")?.trim().orEmpty()

        // Trim from the front so an over-long name keeps its extension (the part that matters).
        if (name.length > MAX_FILE_NAME_LENGTH) {
            name = "..." + name.takeLast(MAX_FILE_NAME_LENGTH - 3)
        }
        if (name.isEmpty() || name.all { it == '.' }) name = "upload"
        return name
    }

    private fun uniqueTarget(dir: File, name: String): File {
        var target = File(dir, name)
        if (!target.exists()) return target
        val dot = name.lastIndexOf('.')
        val stem = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var n = 1
        while (true) {
            target = File(dir, "$stem ($n)$ext")
            if (!target.exists()) return target
            n++
        }
    }
}
