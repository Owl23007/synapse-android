plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "top.contins.synapse.feature.task"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    // Lifecycle & Basic Android
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.android.basic)

    // Compose LiveData integration
    implementation("androidx.compose.runtime:runtime-livedata:1.9.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.testing)
}
