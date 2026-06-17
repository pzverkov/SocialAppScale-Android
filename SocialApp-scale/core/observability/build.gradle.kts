plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
}

android {
    namespace = "com.pzverkov.socialapp.core.observability"
}

dependencies {
    implementation(project(":core:common")) // StoreInterceptor
    implementation(project(":core:domain")) // CrashReporter

    testImplementation(libs.junit)
}
