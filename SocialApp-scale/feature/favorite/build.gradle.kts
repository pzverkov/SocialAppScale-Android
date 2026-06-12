plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pzverkov.socialapp.feature.favorite"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
