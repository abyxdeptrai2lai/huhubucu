@file:Suppress("UnstableApiUsage")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven { url = uri("https://maven.aliucord.com/releases") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.aliucord.com/releases") }
        maven { url = uri("https://maven.aliucord.com/snapshots") }
    }
}
rootProject.name = "PasteAndSpam"
include(":plugin")
