import org.gradle.jvm.toolchain.JavaLanguageVersion

// Screenshot-only module for :feature:itemdetail's AI/translation cards. It lives apart from the
// feature so its Robolectric coverage data never reaches the kover aggregate (which would perturb
// the feature's ViewModel coverage). :app excludes this module from the coverage report.
plugins {
    id("socialapp.android.library.compose")
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.pzverkov.socialapp.screenshot.itemdetail"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// Roborazzi renders under Robolectric at SDK 36, which needs a JDK 21 runtime.
tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) },
    )
}

dependencies {
    implementation(project(":feature:itemdetail"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
