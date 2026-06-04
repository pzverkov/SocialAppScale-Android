plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pzverkov.socialapp.core.network"
    buildFeatures {
        // BuildConfig.DEBUG gates the HTTP logging interceptor level.
        buildConfig = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
}
