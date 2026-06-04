plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
    alias(libs.plugins.kover)
}

android {
    namespace = "com.pzverkov.socialapp.core.sharing"
}

dependencies {
    testImplementation(libs.junit)
}
