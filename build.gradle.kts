plugins {
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.aliucord.plugin") version "1.0.0" apply false
}

subprojects {
    apply(plugin = "com.aliucord.plugin")
}
