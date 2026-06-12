plugins {
    id("socialapp.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pzverkov.socialapp.feature.itemlist"
}

dependencies {
    implementation(project(":core:sharing"))
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
