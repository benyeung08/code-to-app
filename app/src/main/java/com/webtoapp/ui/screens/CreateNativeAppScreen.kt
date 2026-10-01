package com.webtoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.ui.components.WtaCodeEditorDialog
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private data class NativeFile(val path: String, val language: String, val label: String)

private val nativeFiles = listOf(
    NativeFile("app/src/main/java/com/example/mobilecode/MainActivity.kt", "Kotlin", "MainActivity.kt"),
    NativeFile("app/src/main/java/com/example/mobilecode/NativeBridge.java", "Java", "NativeBridge.java"),
    NativeFile("app/src/main/AndroidManifest.xml", "XML", "AndroidManifest.xml"),
    NativeFile("app/src/main/res/layout/activity_main.xml", "XML", "activity_main.xml"),
    NativeFile("app/src/main/res/values/strings.xml", "XML", "strings.xml"),
    NativeFile("app/src/main/res/values/styles.xml", "XML", "styles.xml")
)

private fun defaultNativeSource(file: NativeFile, packageName: String): String = when (file.path) {
    nativeFiles[0].path -> """package $packageName

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
"""
    nativeFiles[1].path -> """package $packageName;

import android.content.Context;
import android.widget.Toast;

public final class NativeBridge {
    private NativeBridge() {}

    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
"""
    nativeFiles[2].path -> """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:theme="@style/AppTheme" android:label="@string/app_name">
        <activity android:name=".${packageName.substringAfterLast('.')}.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"""
    nativeFiles[3].path -> """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/hello_native"
        android:textSize="22sp" />
</LinearLayout>
"""
    nativeFiles[4].path -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Mobile Native App</string>
    <string name="hello_native">Hello from Kotlin + Java</string>
</resources>
"""
    else -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="AppTheme" parent="android:style/Theme.Material.Light.NoActionBar" />
</resources>
"""
}

@Composable
fun CreateNativeAppScreen(onBack: () -> Unit) {
    var projectName by remember { mutableStateOf("NativeMobileApp") }
    var packageName by remember { mutableStateOf("com.example.mobilecode") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showEditor by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val context = LocalContext.current
    val source = remember { mutableStateOf(nativeFiles.associateWith { defaultNativeSource(it, packageName) }) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val resolver = context.contentResolver
                resolver.openOutputStream(uri)?.use { output ->
                    ZipOutputStream(output).use { zip ->
                        val files = linkedMapOf(
                            "settings.gradle" to "pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }\n\nrootProject.name = \"$projectName\"\ninclude ':app'\n",
                            "build.gradle" to "plugins { id 'com.android.application' version '8.9.0' apply false }\n",
                            "app/build.gradle" to "plugins { id 'com.android.application' }\n\nandroid { namespace '$packageName'; compileSdk 35\n    defaultConfig { applicationId '$packageName'; minSdk 23; targetSdk 35; versionCode 1; versionName '1.0' }\n}\n\ndependencies { implementation 'androidx.appcompat:appcompat:1.7.0' }\n",
                            "README.md" to "# $projectName\n\nGenerated on-device by MobileCodeToApp. Edit Kotlin and Java, then open this project in Android Studio or another Android Gradle environment to build the APK.\n"
                        )
                        source.value.forEach { (file, content) -> files[file.path] = content }
                        files.forEach { (path, content) ->
                            zip.putNextEntry(ZipEntry(path))
                            zip.write(content.toByteArray())
                            zip.closeEntry()
                        }
                    }
                } ?: error("Cannot open output")
            }.onSuccess { status = "專案 ZIP 已匯出：可用 Android Studio 編譯 APK" }
                .onFailure { status = "匯出失敗：${it.message}" }
        }
    }

    WtaCreateFlowScaffold(
        title = "原生 Android 專案（Kotlin + Java）",
        onBack = onBack,
        actions = {
            Button(onClick = { exportLauncher.launch("$projectName-native-project.zip") }) {
                Text("匯出專案")
            }
        }
    ) {
        Text(
            "在手機上編寫 Kotlin、Java、Manifest 和 XML 資源。此版本匯出標準 Android Gradle 專案，使用 Android Studio 或已配置 Android SDK 的環境建置 APK。",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it.filter { c -> c.isLetterOrDigit() || c == '_' } },
            label = { Text("專案名稱") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = packageName,
            onValueChange = { packageName = it.filter { c -> c.isLetterOrDigit() || c == '.' || c == '_' } },
            label = { Text("Package 名稱") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("原生檔案", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = nativeFiles[selectedIndex].language == "Kotlin", onClick = { selectedIndex = 0 }, label = { Text("Kotlin") })
                    FilterChip(selected = nativeFiles[selectedIndex].language == "Java", onClick = { selectedIndex = 1 }, label = { Text("Java") })
                    FilterChip(selected = nativeFiles[selectedIndex].language == "XML", onClick = { selectedIndex = 2 }, label = { Text("XML / Manifest") })
                }
                nativeFiles.filter { it.language == nativeFiles[selectedIndex].language }.forEach { file ->
                    Button(onClick = { selectedIndex = nativeFiles.indexOf(file); showEditor = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(file.label)
                    }
                }
            }
        }
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary)
    }

    if (showEditor) {
        val file = nativeFiles[selectedIndex]
        WtaCodeEditorDialog(
            language = file.language,
            initialContent = source.value[file].orEmpty(),
            placeholder = "在手機輸入 ${file.label}",
            onSave = { content ->
                source.value = source.value + (file to content)
                showEditor = false
            },
            onDismiss = { showEditor = false },
            canSaveEmpty = false
        )
    }
}
