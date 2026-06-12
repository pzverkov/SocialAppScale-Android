plugins {
    id("socialapp.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pzverkov.socialapp.feature.itemdetail"
}

dependencies {
    implementation(project(":core:sharing"))
    implementation(project(":core:navigation"))

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
