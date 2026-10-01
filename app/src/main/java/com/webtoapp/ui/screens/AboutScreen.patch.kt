
// AboutScreen.kt 修改點
// 原本:
val version = BuildConfig.VERSION_NAME // 顯示 v1.0.0-mobilecode
// 改為:
val version = BuildConfig.VERSION_NAME // 現在是 1.0.1

// 刷新按鈕 onClick:
IconButton(onClick = {
    scope.launch {
        checking = true
        val result = UpdateChecker.checkOwnUpdate()
        showDialog = result
        checking = false
    }
})

// Dialog:
if (result.hasUpdate) {
    Text("發現新版本 ${result.latestVersion}")
    Text(result.releaseNotes)
    Button(onClick = {
        scope.launch {
            val file = UpdateChecker.downloadApk(context, result.downloadUrl!!) { p -> progress = p }
            UpdateChecker.installApk(context, file)
        }
    }) { Text("立即更新") }
}

// 歷史版本:
IconButton(onClick = {
    scope.launch {
        val all = UpdateChecker.fetchAllReleases()
        showHistory = all
    }
})
