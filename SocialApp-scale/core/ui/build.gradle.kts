import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("socialapp.android.library.compose")
    id("socialapp.android.metro")
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.pzverkov.socialapp.core.ui"

    testOptions {
        unitTests {
            // Robolectric needs merged Android resources to render the design system.
            isIncludeAndroidResources = true
        }
    }
}

// Roborazzi screenshot tests render under Robolectric at SDK 36, which requires a JDK 21 runtime.
// Modules compile to JVM 17; only this module's unit test task runs on 21.
tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) },
    )
}

dependencies {
    api(project(":core:model"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Screenshot tests: Roborazzi renders Compose under Robolectric on the JVM (no emulator).
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
}
