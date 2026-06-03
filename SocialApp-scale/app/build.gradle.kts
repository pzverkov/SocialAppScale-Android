import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.metro)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

kover {
    reports {
        filters {
            excludes {
                packages(
                    "com.pzverkov.socialapp.core.di", // Metro graph + ViewModel factory wiring
                    "com.pzverkov.socialapp.core.ui.theme",
                    "com.pzverkov.socialapp.core.ui.components",
                    "com.pzverkov.socialapp.core.navigation",
                )
                classes(
                    "*BuildConfig",
                    "*.MainActivity",
                    "*.SocialAppApplication",
                    "*.InstallationIdProviderImpl", // Requires Android Context, tested via instrumentation
                    // Metro-generated DI plumbing (no hand-written logic to test).
                    "*MetroFactory*",
                    "*BindsMirror*",
                    "*MetroContribution*",
                )
                annotatedBy(
                    "dev.zacsweers.metro.DependencyGraph", // graph declarations
                    "dev.zacsweers.metro.ContributesTo", // @Provides container interfaces
                    "androidx.compose.runtime.Composable",
                )
            }
        }
        verify {
            rule("Global minimum 65% line coverage") {
                bound {
                    minValue = 65
                    coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
                    aggregationForGroup = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
            rule("Global minimum 60% branch coverage") {
                bound {
                    minValue = 60
                    coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
                    aggregationForGroup = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

// Release signing credentials are read from a gitignored keystore.properties,
// falling back to environment variables for CI. Secrets never live in the repo.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}
fun signingValue(key: String, env: String): String? =
    keystoreProps.getProperty(key) ?: System.getenv(env)
val releaseStorePath = signingValue("storeFile", "KEYSTORE_FILE")

android {
    namespace = "com.pzverkov.socialapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pzverkov.socialapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.pzverkov.socialapp.MetroTestRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseStorePath != null) {
                storeFile = file(releaseStorePath)
                storePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Use the release keystore when configured; otherwise produce an
            // unsigned APK rather than silently shipping a debug-signed build.
            signingConfig = if (releaseStorePath != null) {
                signingConfigs.getByName("release")
            } else {
                logger.warn("No release keystore configured; release build will be unsigned.")
                null
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = true
        lintConfig = file("lint.xml")
        htmlReport = true
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // DI: Metro (compiler plugin auto-adds runtime) + AndroidX ViewModel integration
    implementation(libs.metrox.viewmodel.compose)

    // Image loading
    implementation(libs.coil.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Instrumentation testing
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.test.rules)
}
