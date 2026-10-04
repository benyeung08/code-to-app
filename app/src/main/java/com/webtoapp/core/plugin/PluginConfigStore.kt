package com.webtoapp.core.plugin

import android.content.Context
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.GsonProvider
import java.io.File

/**
 * Persistent key-value storage behind `hcj.config.*`.
 *
 * One JSON file per plugin under `files/plugin_config/<pluginId>.json`.
 * Values are stored as raw JSON snippets so plugin code can round-trip any
 * JSON-serializable value; access is process-wide synchronized — files are
 * small and writes are infrequent.
 *
 * Shell-synced: generated APKs persist plugin config the same way.
 */
class PluginConfigStore(private val context: Context) {

    companion object {
        private const val TAG = "PluginConfigStore"
        private const val DIR = "plugin_config"
        private val gson get() = GsonProvider.gson
    }

    private fun fileFor(pluginId: String): File {
        val safe = pluginId.replace(Regex("[^a-zA-Z0-9_\\-.]"), "_")
        return File(File(context.filesDir, DIR).apply { mkdirs() }, "$safe.json")
    }

    @Synchronized
    fun get(pluginId: String, key: String): String? {
        val obj = readObject(pluginId) ?: return null
        val el = obj.get(key) ?: return null
        return if (el.isJsonNull) null else gson.toJson(el)
    }

    @Synchronized
    fun set(pluginId: String, key: String, jsonValue: String) {
        val obj = readObject(pluginId) ?: com.google.gson.JsonObject()
        try {
            obj.add(key, JsonParser.parseString(jsonValue))
        } catch (e: Exception) {
            obj.addProperty(key, jsonValue)
        }
        writeObject(pluginId, obj)
    }

    @Synchronized
    fun remove(pluginId: String, key: String) {
        val obj = readObject(pluginId) ?: return
        obj.remove(key)
        writeObject(pluginId, obj)
    }

    @Synchronized
    fun all(pluginId: String): String {
        return gson.toJson(readObject(pluginId) ?: com.google.gson.JsonObject())
    }

    @Synchronized
    fun clear(pluginId: String) {
        fileFor(pluginId).delete()
    }

    private fun readObject(pluginId: String): com.google.gson.JsonObject? {
        val file = fileFor(pluginId)
        if (!file.exists()) return null
        return try {
            JsonParser.parseString(file.readText()).asJsonObject
        } catch (e: Exception) {
            AppLogger.w(TAG, "corrupt config for $pluginId, resetting")
            null
        }
    }

    private fun writeObject(pluginId: String, obj: com.google.gson.JsonObject) {
        try {
            fileFor(pluginId).writeText(gson.toJson(obj))
        } catch (e: Exception) {
            AppLogger.e(TAG, "failed to write config for $pluginId", e)
        }
    }
}
