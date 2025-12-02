pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.google.com") }
        resolutionStrategy {
            eachPlugin {
                if (requested.id.id == "dagger.hilt.android.plugin") {
                    useModule("com.google.dagger:hilt-android-gradle-plugin:2.57.1")
                }
            }
        }
    }
}

rootProject.name = "Synapse"
include(":app")
include(":data")
include(":domain")
include(":feature:task")
include(":feature:schedule")
include(":feature:goal")
include(":feature:assistant")
include(":network")

