// app/build.gradle.kts —— benyeung08/web-to-app
// 改动：
//   1. 新增 id("org.jetbrains.kotlin.plugin.compose")
//   2. 删除 composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }（Kotlin 1.9 时代写法，配 Kotlin 2.2.10 会直接报错）
//   3. Compose BOM 2024.12.01 → 2026.06.01（与上游对齐，避免 compiler/runtime 版本不匹配）

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.webtoapp.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 101
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // ⚠️ 已删除：composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
    // Kotlin 2.x 的 Compose 编译器版本 = Kotlin 版本（此处即 2.2.10），由
    // org.jetbrains.kotlin.plugin.compose 插件自动带入，不能再手动指定。

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("standard") {
            dimension = "mode"
            isDefault = true
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.jakewharton.timber:timber:5.0.1")
}
