package com.webtoapp.data.model

/**
 * Resolved visibility of each toolbar control in the shell/preview browser toolbar.
 *
 * Semantics: the "browser toolbar" master switch (`browserToolbarEnabled`) decides
 * whether the toolbar renders at all — off means the page runs with no bar at all.
 * When enabled, each `toolbarShow*` flag controls its own button. The editor flips
 * all flags together with the master switch, so a fresh "on" state is all-on and a
 * fresh "off" state is all-off.
 */
data class ToolbarButtonVisibility(
    val showTitle: Boolean,
    val showUrl: Boolean,
    val showBack: Boolean,
    val showForward: Boolean,
    val showRefresh: Boolean,
    val showConsoleButton: Boolean,
    val showFind: Boolean
)

/**
 * Whether the enabled toolbar has at least one item to render. Console is a toolbar
 * item in its own right: a toolbar customized down to only that button must still
 * render. Both the host preview and the exported shell gate the toolbar on this
 * predicate — keep them on the shared function so the two paths cannot drift apart.
 * The console/find flags have NO defaults on purpose: every caller must pass them
 * explicitly, so a new flag can never be silently treated as "on" by one path and
 * not the other.
 */
fun hasAnyToolbarItem(
    toolbarShowTitle: Boolean,
    toolbarShowUrl: Boolean,
    toolbarShowBack: Boolean,
    toolbarShowForward: Boolean,
    toolbarShowRefresh: Boolean,
    toolbarShowConsole: Boolean,
    toolbarShowFind: Boolean
): Boolean = toolbarShowTitle || toolbarShowUrl || toolbarShowBack || toolbarShowForward ||
    toolbarShowRefresh || toolbarShowConsole || toolbarShowFind

/**
 * Resolves which toolbar buttons are visible. Every button requires BOTH the master
 * switch and its own flag. The console/find flags have NO defaults on purpose — same
 * anti-drift rule as [hasAnyToolbarItem].
 */
fun resolveToolbarButtons(
    toolbarEnabled: Boolean,
    toolbarShowTitle: Boolean,
    toolbarShowUrl: Boolean,
    toolbarShowBack: Boolean,
    toolbarShowForward: Boolean,
    toolbarShowRefresh: Boolean,
    toolbarShowConsole: Boolean,
    toolbarShowFind: Boolean
): ToolbarButtonVisibility = ToolbarButtonVisibility(
    showTitle = toolbarEnabled && toolbarShowTitle,
    showUrl = toolbarEnabled && toolbarShowUrl,
    showBack = toolbarEnabled && toolbarShowBack,
    showForward = toolbarEnabled && toolbarShowForward,
    showRefresh = toolbarEnabled && toolbarShowRefresh,
    showConsoleButton = toolbarEnabled && toolbarShowConsole,
    showFind = toolbarEnabled && toolbarShowFind
)
