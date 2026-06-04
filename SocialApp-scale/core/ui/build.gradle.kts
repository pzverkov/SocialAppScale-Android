plugins {
    id("socialapp.android.library.compose")
    id("socialapp.android.metro")
}

android {
    namespace = "com.pzverkov.socialapp.core.ui"
}

dependencies {
    api(project(":core:model"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
