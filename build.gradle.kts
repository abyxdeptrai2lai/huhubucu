buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/releases")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        // Đã xoá dòng classpath của aliucord vì nó không có trên maven
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/releases")
    }
}
