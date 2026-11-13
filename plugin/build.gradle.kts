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
    // Cấu hình để nó tự đóng gói file zip giống Aliucord
    libraryVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "PasteAndSpam.zip"
        }
    }
}

dependencies {
    compileOnly("com.discord:discord:126021")
}
