import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// The YouTube key comes from local.properties (git-ignored), or the YOUTUBE_API_KEY
// environment variable. It is never committed. See README.md.
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.isFile) file.inputStream().use { load(it) }
    }
val youtubeApiKey: String =
    localProperties
        .getProperty("YOUTUBE_API_KEY")
        ?.trim()
        .orEmpty()
        .ifEmpty { providers.environmentVariable("YOUTUBE_API_KEY").orNull?.trim().orEmpty() }

if (youtubeApiKey.isEmpty()) {
    logger.warn(
        "WARNING: YOUTUBE_API_KEY is missing from local.properties. " +
            "Debug builds use an empty key and YouTube features will show \"YouTube key missing\". " +
            "Release builds will fail until the key is added."
    )
}

android {
    namespace = "dev.aladins.focustube"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "dev.aladins.focustube"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val escapedKey = youtubeApiKey.replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "YOUTUBE_API_KEY", "\"$escapedKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// Release builds must carry a real key; debug builds only warn (see above).
val checkYoutubeKey =
    tasks.register("checkYoutubeKey") {
        val hasKey = youtubeApiKey.isNotEmpty()
        doLast {
            if (!hasKey) {
                throw GradleException(
                    "YOUTUBE_API_KEY is missing. Add YOUTUBE_API_KEY=... to local.properties before building a release."
                )
            }
        }
    }
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(checkYoutubeKey) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
