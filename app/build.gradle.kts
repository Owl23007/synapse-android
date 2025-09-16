plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "top.contins.synapse"
    compileSdk = 35

    defaultConfig {
        applicationId = "top.contins.synapse"
        minSdk = 29
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

    buildFeatures {
        compose = true
    }

    // 配置源码目录结构
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            kotlin.srcDirs("src/main/kotlin")
        }
    }
}

// Hilt 配置
hilt {
    enableAggregatingTask = false
}

dependencies {
    // BOM
    implementation(platform(libs.compose.bom))

    // Android Basic
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    // Hilt 依赖
    implementation(libs.hilt.android)
    implementation(libs.window)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Compose UI
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.bundles.compose)

    // Module dependencies
    implementation(project(":data"))
    implementation(project(":network"))
    implementation(project(":domain"))
    implementation(project(":feature:assistant"))
    implementation(project(":feature:goal"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:task"))
    implementation(project(":feature:writing"))

    debugImplementation(libs.compose.ui.tooling)

    // 网络库依赖
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Markdown渲染
    implementation(libs.markdown.compose)

}