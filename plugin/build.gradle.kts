android {
    namespace = "com.aliucord.plugins.pasteandspam"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

aliucord {
    author("Freddy")
    description = "Paste button and Spam menu"
    version = "1.0.0"
    deploy.set(true)
}

dependencies {
    compileOnly("com.discord:discord:126021")
    compileOnly("com.aliucord:Aliucord:2.9.1") // BẮT BUỘC
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.5.21") // Đúng version của Aliucord
}
