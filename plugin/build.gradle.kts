version = "1.0.0"
description = "Paste button and Spam menu"

aliucord {
    changelog.set("""
        # 1.0.0
        * Initial release!
    """.trimIndent())
    deploy.set(true) // Bật để tự động đẩy file zip lên nhánh builds
}

dependencies {
    compileOnly("com.discord:discord:126021")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.5.21") // Đúng phiên bản stdlib của Aliucord
}
