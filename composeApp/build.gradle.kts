import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            // viewModel() composable: solo existe variante Android
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.ktx)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)
            implementation("androidx.work:work-runtime-ktx:2.11.1")
        }

        getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}


// lifecycle-runtime-ktx es androidJvm-only; lifecycle-viewmodel-desktop lo jala
// transitivamente. Se sustituye por lifecycle-runtime que sí tiene variante JVM.
configurations.configureEach {
    if (name.contains("desktop", ignoreCase = true)) {
        resolutionStrategy.dependencySubstitution {
            substitute(module("androidx.lifecycle:lifecycle-runtime-ktx"))
                .using(module("androidx.lifecycle:lifecycle-runtime:${libs.versions.lifecycle.get()}"))
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    debugImplementation(compose.uiTooling)
}

android {
    namespace = "com.example.sicedroidmultiplatform"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sicedroidmultiplatform"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/main/res")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.sicedroidmultiplatform.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SICEDroidMultiplatform"
            packageVersion = "1.0.0"
        }
    }
}
