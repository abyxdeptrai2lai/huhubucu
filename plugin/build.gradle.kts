plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.aliucord.plugin")
}

android {
    namespace = "com.aliucord.plugins.pasteandspam"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }
}

aliucord {
    author("Freddy")
    description("Paste button and Spam menu")
    version = "1.0.0"
}

dependencies {
    compileOnly("com.discord:discord:126021")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
}
