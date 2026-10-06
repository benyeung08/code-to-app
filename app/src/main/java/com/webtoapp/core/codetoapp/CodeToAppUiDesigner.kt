package com.webtoapp.core.codetoapp

import org.json.JSONArray
import org.json.JSONObject

/**
 * 可视化 UI 设计器的资料模型。
 *
 * 刻意用「节点树 + 属性字典」而不是每种元件一个 sealed class：
 * 元件种类会持续增加，属性字典让新增元件只需在 [UiComponentType] 加一笔
 * 并描述它的属性，序列化／反序列化／属性面板三处都不用改。
 */
object CodeToAppUiDesigner {

    // ---------------------------------------------------------------- 节点

    data class UiNode(
        val id: String,
        val type: UiComponentType,
        val props: MutableMap<String, String> = mutableMapOf(),
        val children: MutableList<UiNode> = mutableListOf()
    ) {
        fun find(targetId: String): UiNode? {
            if (id == targetId) return this
            for (child in children) {
                child.find(targetId)?.let { return it }
            }
            return null
        }

        /** 回传父节点；根节点回传 null。 */
        fun parentOf(targetId: String): UiNode? {
            for (child in children) {
                if (child.id == targetId) return this
                child.parentOf(targetId)?.let { return it }
            }
            return null
        }

        fun remove(targetId: String): Boolean {
            val it = children.iterator()
            while (it.hasNext()) {
                val child = it.next()
                if (child.id == targetId) {
                    it.remove()
                    return true
                }
                if (child.remove(targetId)) return true
            }
            return false
        }

        fun countNodes(): Int = 1 + children.sumOf { it.countNodes() }
    }

    // ---------------------------------------------------------------- 元件种类

    enum class UiComponentType(
        val labelKey: String,
        val canHaveChildren: Boolean,
        val defaultProps: Map<String, String> = emptyMap()
    ) {
        COLUMN("ctaUiColumn", true, mapOf("padding" to "16", "gap" to "8", "background" to "#00000000")),
        ROW("ctaUiRow", true, mapOf("padding" to "8", "gap" to "8", "background" to "#00000000")),
        CARD("ctaUiCard", true, mapOf("padding" to "16", "gap" to "8", "radius" to "12", "background" to "#FFFFFFFF")),
        TEXT("ctaUiText", false, mapOf(
            "text" to "Hello CodeToApp", "size" to "16", "color" to "#FF000000",
            "weight" to "normal", "align" to "start"
        )),
        BUTTON("ctaUiButton", false, mapOf(
            "text" to "Click me", "background" to "#FF6200EE", "color" to "#FFFFFFFF",
            "radius" to "8", "size" to "15"
        )),
        INPUT("ctaUiInput", false, mapOf(
            "hint" to "Type here", "size" to "15", "color" to "#FF000000", "radius" to "8"
        )),
        IMAGE("ctaUiImage", false, mapOf("src" to "", "height" to "160", "radius" to "8")),
        CHECKBOX("ctaUiCheckbox", false, mapOf("text" to "Checkbox", "checked" to "false", "color" to "#FF000000")),
        SWITCH("ctaUiSwitch", false, mapOf("text" to "Switch", "checked" to "false", "color" to "#FF6200EE")),
        DIVIDER("ctaUiDivider", false, mapOf("color" to "#FFBDBDBD", "height" to "1")),
        SPACER("ctaUiSpacer", false, mapOf("height" to "16")),
        ;

        fun isContainer(): Boolean = canHaveChildren
    }

    // ---------------------------------------------------------------- 属性定义

    enum class PropKind { TEXT, NUMBER, COLOR, SELECT, BOOLEAN }

    data class PropDef(
        val key: String,
        val labelKey: String,
        val kind: PropKind,
        val options: List<String> = emptyList()
    )

    /** 每种元件可编辑的属性；属性面板与 HTML 产生器都读这张表。 */
    fun propsFor(type: UiComponentType): List<PropDef> = when (type) {
        UiComponentType.COLUMN, UiComponentType.ROW, UiComponentType.CARD -> listOf(
            PropDef("padding", "ctaUiPropPadding", PropKind.NUMBER),
            PropDef("gap", "ctaUiPropGap", PropKind.NUMBER),
            PropDef("radius", "ctaUiPropRadius", PropKind.NUMBER),
            PropDef("background", "ctaUiPropBackground", PropKind.COLOR)
        )
        UiComponentType.TEXT -> listOf(
            PropDef("text", "ctaUiPropText", PropKind.TEXT),
            PropDef("size", "ctaUiPropSize", PropKind.NUMBER),
            PropDef("color", "ctaUiPropColor", PropKind.COLOR),
            PropDef("weight", "ctaUiPropWeight", PropKind.SELECT, listOf("normal", "bold", "lighter")),
            PropDef("align", "ctaUiPropAlign", PropKind.SELECT, listOf("start", "center", "end"))
        )
        UiComponentType.BUTTON -> listOf(
            PropDef("text", "ctaUiPropText", PropKind.TEXT),
            PropDef("size", "ctaUiPropSize", PropKind.NUMBER),
            PropDef("color", "ctaUiPropColor", PropKind.COLOR),
            PropDef("background", "ctaUiPropBackground", PropKind.COLOR),
            PropDef("radius", "ctaUiPropRadius", PropKind.NUMBER)
        )
        UiComponentType.INPUT -> listOf(
            PropDef("hint", "ctaUiPropHint", PropKind.TEXT),
            PropDef("size", "ctaUiPropSize", PropKind.NUMBER),
            PropDef("color", "ctaUiPropColor", PropKind.COLOR),
            PropDef("radius", "ctaUiPropRadius", PropKind.NUMBER)
        )
        UiComponentType.IMAGE -> listOf(
            PropDef("src", "ctaUiPropSrc", PropKind.TEXT),
            PropDef("height", "ctaUiPropHeight", PropKind.NUMBER),
            PropDef("radius", "ctaUiPropRadius", PropKind.NUMBER)
        )
        UiComponentType.CHECKBOX, UiComponentType.SWITCH -> listOf(
            PropDef("text", "ctaUiPropText", PropKind.TEXT),
            PropDef("checked", "ctaUiPropChecked", PropKind.BOOLEAN),
            PropDef("color", "ctaUiPropColor", PropKind.COLOR)
        )
        UiComponentType.DIVIDER -> listOf(
            PropDef("color", "ctaUiPropColor", PropKind.COLOR),
            PropDef("height", "ctaUiPropHeight", PropKind.NUMBER)
        )
        UiComponentType.SPACER -> listOf(
            PropDef("height", "ctaUiPropHeight", PropKind.NUMBER)
        )
    }

    // ---------------------------------------------------------------- 预设

    private var idCounter = 0

    fun newId(): String = "n${System.currentTimeMillis().toString(36)}${idCounter++}"

    fun createNode(type: UiComponentType): UiNode =
        UiNode(newId(), type, type.defaultProps.toMutableMap())

    fun emptyRoot(): UiNode = UiNode(
        id = "root",
        type = UiComponentType.COLUMN,
        props = mutableMapOf("padding" to "16", "gap" to "8", "background" to "#00000000")
    )

    // ---------------------------------------------------------------- 序列化

    fun toJson(root: UiNode): JSONObject = JSONObject().apply {
        put("version", 1)
        put("root", nodeToJson(root))
    }

    private fun nodeToJson(node: UiNode): JSONObject = JSONObject().apply {
        put("id", node.id)
        put("type", node.type.name)
        val p = JSONObject()
        node.props.forEach { (k, v) -> p.put(k, v) }
        put("props", p)
        val arr = JSONArray()
        node.children.forEach { arr.put(nodeToJson(it)) }
        put("children", arr)
    }

    fun fromJson(json: String): UiNode? = runCatching {
        parseNode(JSONObject(json).getJSONObject("root"))
    }.getOrNull()

    private fun parseNode(obj: JSONObject): UiNode {
        val type = runCatching { UiComponentType.valueOf(obj.optString("type")) }
            .getOrDefault(UiComponentType.TEXT)
        val props = mutableMapOf<String, String>()
        val p = obj.optJSONObject("props")
        if (p != null) {
            val it = p.keys()
            while (it.hasNext()) {
                val k = it.next()
                props[k] = p.optString(k, "")
            }
        }
        val node = UiNode(obj.optString("id").ifBlank { newId() }, type, props)
        val arr = obj.optJSONArray("children")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val childObj = arr.optJSONObject(i) ?: continue
                node.children.add(parseNode(childObj))
            }
        }
        return node
    }

    // ---------------------------------------------------------------- 档案管理

    fun layoutFile(projectDir: java.io.File): java.io.File =
        java.io.File(projectDir, "ui-designer.json")

    fun save(projectDir: java.io.File, root: UiNode): Boolean {
        if (!projectDir.exists()) projectDir.mkdirs()
        return runCatching {
            layoutFile(projectDir).writeText(toJson(root).toString(2))
            true
        }.getOrDefault(false)
    }

    fun load(projectDir: java.io.File): UiNode {
        val file = layoutFile(projectDir)
        if (!file.exists()) return emptyRoot()
        return fromJson(file.readText()) ?: emptyRoot()
    }
}
