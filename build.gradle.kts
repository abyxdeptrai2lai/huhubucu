buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/releases")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("com.aliucord:gradle:1.0.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/releases")
    }
}
