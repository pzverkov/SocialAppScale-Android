import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.JavaVersion

plugins {
    // Version-less: AGP and the baseline-profile plugin are on the classpath via the root build's
    // `apply false` declarations. AGP 9 has built-in Kotlin, so no separate Kotlin plugin.
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.pzverkov.socialapp.baselineprofile"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        // Baseline profile generation runs on API 28+; the app itself still ships minSdk 26.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // The app under instrumentation.
    targetProjectPath = ":app"

    // A managed virtual device lets CI generate the profile with no physical hardware. The ATD
    // (Automated Test Device) image is headless and stripped down, so it runs faster and lighter
    // in CI than a full emulator; API 36 matches the app's compileSdk/targetSdk.
    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("pixel6Api36") {
            device = "Pixel 6"
            apiLevel = 36
            systemImageSource = "aosp-atd"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

baselineProfile {
    managedDevices += "pixel6Api36"
    // CI uses the managed device above; a developer with a device attached can flip this on.
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
