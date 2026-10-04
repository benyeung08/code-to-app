package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.extension.ExtensionModule
import com.webtoapp.util.GsonProvider
import java.util.UUID

class CreateModuleTool : Tool {
    override val name = "CreateModule"
    override val description = """
        Create a new plugin. Provide a module definition JSON: at minimum
        {"name":"...","code":"..."} (use cssCode instead of code for a pure CSS plugin).
        Optional fields include description, runAt (DOCUMENT_START / DOCUMENT_END / DOCUMENT_IDLE),
        urlMatches, permissions, configItems, and sourceType (default CUSTOM). Use GetModule to see
        the installed plugin shape. A new id is assigned automatically.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("module", "Module definition JSON (ExtensionModule shape).", required = true)
    }

    override fun activityDescription(args: JsonObject): String? = "Creating module"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val json = args.get("module")?.asString
            ?: return ToolResult.error("CreateModule: missing `module`.")
        val parsed = runCatching { GsonProvider.gson.fromJson(json, ExtensionModule::class.java) }.getOrNull()
            ?: return ToolResult.error("CreateModule: `module` is not valid JSON.")
        val module = parsed.sanitized().copy(id = UUID.randomUUID().toString())
        if (module.name.isBlank()) {
            return ToolResult.error("CreateModule: module `name` is required.")
        }
        return when (val result = com.webtoapp.core.plugin.PluginImporter(ctx.androidContext)
            .installLegacyModule(module)
        ) {
            is com.webtoapp.core.plugin.PluginImporter.ImportResult.Success ->
                ToolResult.ok("Created plugin id=${result.plugin.id} name=\"${result.plugin.name}\".")
            is com.webtoapp.core.plugin.PluginImporter.ImportResult.Error ->
                ToolResult.error("CreateModule failed: ${result.message}")
        }
    }
}
