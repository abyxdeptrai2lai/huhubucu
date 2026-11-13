plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.aliucord.plugins.pasteandspam"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }
}

dependencies {
    compileOnly("com.discord:discord:126021")
}

tasks.register<Zip>("make") {
    from("src/main")
    archiveFileName.set("PasteAndSpam.zip")
    destinationDirectory.set(layout.buildDirectory.dir("outputs"))
}
