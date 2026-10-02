
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        minSdk = 23
        targetSdk = 36
        // === 自更新修改：你的版本 ===
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        // 原版用於傳遞版本給 shell
        buildConfigField("String", "GIT_COMMIT", "\"${System.getenv("GIT_COMMIT") ?: "dev"}\"")
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
    }

    // === 關鍵：去掉 MobileCode 自動加的 -mobilecode 後綴 ===
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 強制清空後綴
            versionNameSuffix = ""
        }
        debug {
            versionNameSuffix = ""
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    // 原版有兩個 flavor
    flavorDimensions += "market"
    productFlavors {
        create("standard") {
            dimension = "market"
            applicationId = "com.webtoapp"
            versionNameSuffix = "" // 再次強制清空
        }
        create("gplay") {
            dimension = "market"
            applicationId = "shiaho.webtoapp"
            versionNameSuffix = ""
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // bumped for Kotlin 2.3 compatibility, original was 1.5.14
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
        jniLibs {
            // 保留原版 so
            useLegacyPackaging = false
        }
    }

    // === Phase 2：加入 gradle_launcher 的 CMake 編譯 ===
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/gradle_launcher/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // 原版還有 ndkVersion
    ndkVersion = "27.0.11718014"

    // DataStore / Room schema
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // === AndroidX & Compose ===
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // === Koin ===
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    // === Room 2.7.2 + KSP ===
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // === 網絡 & 簽名 (原版核心) ===
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
    implementation("com.android.tools.build:apksig:8.3.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")

    // === Gson (自更新需要) ===
    implementation("com.google.code.gson:gson:2.10.1")

    // === Firebase (原版) ===
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // === Coil, Haze, Vico, ZXing ===
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("dev.chrisbanes.haze:haze:0.7.2")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.23")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")

    // === 壓縮 & 解壓 (導入 AS 專案需要) ===
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.tukaani:xz:1.9")

    // === GeckoView (可選) ===
    implementation("org.mozilla.geckoview:geckoview:131.0.20241010134135")

    // === 測試 ===
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// === 讓 standardRelease 成為自更新的默認產物 ===
// 原版任務
tasks.register("printVersion") {
    doLast {
        println("versionName=${android.defaultConfig.versionName} versionCode=${android.defaultConfig.versionCode}")
    }
}