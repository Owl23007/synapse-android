plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "top.contins.synapse.feature.chat"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
    }

    buildFeatures {
        compose = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":domain"))
    implementation(project(":network"))
    implementation(project(":feature:assistant")) // Chat might depend on assistant feature if they share logic, or maybe not. Let's stick to core and domain for now.

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    
    // Markdown support for chat
    implementation(libs.bundles.markdown)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
