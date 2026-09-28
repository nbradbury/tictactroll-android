plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
}

// The Play upload key lives outside the repo: its path and passwords come from ~/.gradle/gradle.properties.
val uploadStoreFile = providers.gradleProperty("TICTACTROLL_UPLOAD_STORE_FILE").orNull

android {
    namespace = "com.nbradbury.tictactroll"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.nbradbury.tictactroll"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (uploadStoreFile != null) {
            create("upload") {
                storeFile = file(uploadStoreFile)
                storePassword = providers.gradleProperty("TICTACTROLL_UPLOAD_STORE_PASSWORD").get()
                keyAlias = providers.gradleProperty("TICTACTROLL_UPLOAD_KEY_ALIAS").get()
                keyPassword = providers.gradleProperty("TICTACTROLL_UPLOAD_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            // Without the upload key (e.g. on another machine) release builds are left unsigned.
            signingConfig = signingConfigs.findByName("upload")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
