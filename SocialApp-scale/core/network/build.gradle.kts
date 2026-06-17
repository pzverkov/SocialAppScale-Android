plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pzverkov.socialapp.core.network"
    buildFeatures {
        // BuildConfig.DEBUG gates the HTTP logging interceptor level; BuildConfig.BASE_URL
        // points debug at the local mock server and release at the production host.
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000\"")
    }
    buildTypes {
        release {
            buildConfigField("String", "BASE_URL", "\"https://socialapp.app\"")
        }
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
}
