// 根项目 build.gradle.kts
// 修复点：org.jetbrains.kotlin.android 版本 2.3.2（不存在） -> 2.3.21
// 版本约束：Kotlin 2.3.21 要求 Gradle 7.6.3–9.3.0、AGP 8.2.2–9.0.0

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
}

// 若项目使用了 KSP，同样对齐主版本：
// id("com.google.devtools.ksp") version "2.3.21-2.0.2" apply false
