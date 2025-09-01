plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
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

    // 配置源码目录结构
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            kotlin.srcDirs("src/main/kotlin")
        }
    }
}

//  Hilt 配置
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

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose
    implementation(libs.bundles.compose)
    implementation(libs.compose.runtime.livedata)

    // Module dependencies
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":feature:assistant"))
    implementation(project(":feature:goal"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:task"))
    implementation(project(":feature:writing"))

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}