import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = Properties().apply {
    rootProject
        .file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { load(it) }
}
val apiKey: String =
    localProps.getProperty("ADDRESS_SEARCH_API_KEY")
        ?: System.getenv("ADDRESS_SEARCH_API_KEY")
        ?: throw GradleException(
            "Address search API key not found!\n" +
                    "  • in local.properties under ADDRESS_SEARCH_API_KEY\n" +
                    "  • or in env var ADDRESS_SEARCH_API_KEY"
        )


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.codingfeline.buildkonfig") version "0.17.1"
    kotlin("native.cocoapods")

}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    cocoapods {
        version = "1.0.0"
    }
    
    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.maps.compose)

            // Ktor for geocoding logic
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Ktor engine
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            //location
            implementation(libs.permissions.android)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            api(libs.kmm.viewmodel.core)
            //location
            implementation(libs.permissions)
            implementation(libs.permissions.location)
            api(libs.permissions.compose)
            //extended icons
           //implementation(libs.androidx.material.icons.extended)
        }

        iosMain.dependencies {
                implementation(compose.ui)
                // Ktor engine
                implementation(libs.ktor.client.darwin)
            }
        }
}

buildkonfig {
    packageName = "dev.sahildesai.maps"          // where your generated object will live
    defaultConfigs {
        // inject your apiKey (read from localProps/env as you already do)
        buildConfigField(
            type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            name = "ADDRESS_SEARCH_API_KEY",
            value = apiKey
        )
    }
}

android {
    namespace = "dev.sahildesai.maps"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.sahildesai.maps"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders += mapOf(
            "GOOGLE_MAPS_API_KEY" to apiKey
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true               // ← generate BuildConfig in this module
    }
}

dependencies {
    implementation(libs.play.services.maps)
    implementation(libs.androidx.material3.android)
    implementation(libs.play.services.location)
    debugImplementation(compose.uiTooling)
}

val frameworkName = "ComposeApp"

tasks.register("assembleSharedXCFramework", Exec::class) {
    group = "build"
    description = "Manually assembles XCFramework from device and simulator frameworks"

    dependsOn(
        "linkSharedReleaseFrameworkIosArm64",
        "linkSharedReleaseFrameworkIosSimulatorArm64"
    )

    val arm64Output = buildDir.resolve("bin/iosArm64/ComposeAppReleaseFramework/$frameworkName.framework")
    val simArm64Output = buildDir.resolve("bin/iosSimulatorArm64/ComposeAppReleaseFramework/$frameworkName.framework")
    val outputDir = buildDir.resolve("XCFrameworks/release")

    doFirst {
        outputDir.mkdirs()
    }

    commandLine = listOf(
        "xcodebuild", "-create-xcframework",
        "-framework", arm64Output.absolutePath,
        "-framework", simArm64Output.absolutePath,
        "-output", outputDir.resolve("$frameworkName.xcframework").absolutePath
    )
}

