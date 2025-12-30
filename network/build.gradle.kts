plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "top.contins.synapse.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
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

    // 设置 Java 编译目标为 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Hilt 配置
hilt {
    enableAggregatingTask = false
}

dependencies {
    // Android 基础依赖
    implementation(libs.appcompat)

    // Lifecycle
    implementation(libs.bundles.lifecycle)


    // 网络库
    implementation(libs.bundles.network)


    // 依赖data模块用于TokenManager
    // implementation(project(":core:common"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.conscrypt:conscrypt-android:2.5.3")

    implementation(libs.javax.inject)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}