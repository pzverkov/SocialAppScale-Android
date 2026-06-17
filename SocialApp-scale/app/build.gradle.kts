import java.util.Properties

plugins {
    id("socialapp.android.application")
    alias(libs.plugins.metro)
}

kover {
    reports {
        filters {
            excludes {
                packages(
                    "com.pzverkov.socialapp.core.di", // Metro graph + ViewModel factory wiring
                    "com.pzverkov.socialapp.core.navigation",
                    "com.pzverkov.socialapp.core.ui.theme", // Compose design tokens, covered by instrumentation
                    // Per-feature nav graph wiring, covered by instrumentation (deeplink + navigation tests).
                    "com.pzverkov.socialapp.feature.itemlist.navigation",
                    "com.pzverkov.socialapp.feature.itemdetail.navigation",
                )
                classes(
                    "*BuildConfig",
                    "*.MainActivity",
                    "*.SocialAppApplication",
                    "*.InstallationIdProviderImpl", // Requires Android Context, tested via instrumentation
                    "*.LogcatCrashReporter", // Thin android.util.Log wrapper, not JVM-unit-testable
                    // Metro-generated DI plumbing (no hand-written logic to test).
                    "*MetroFactory*",
                    "*BindsMirror*",
                    "*MetroContribution*",
                    "*_Impl", // Room-generated DAO/database implementations
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

    defaultConfig {
        applicationId = "com.pzverkov.socialapp"
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

    buildFeatures {
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
    // Modules
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":core:sharing"))
    implementation(project(":core:observability"))
    implementation(project(":feature:itemlist"))
    implementation(project(":feature:itemdetail"))
    implementation(project(":feature:favorite"))

    // Aggregate every other module into the app's coverage report so the verification
    // floor measures their code too. Reading subproject paths and declaring a dependency
    // by path holds no cross-project state, so this stays config-cache safe. :core:testing
    // is test-only scaffolding and is left out of the denominator.
    rootProject.subprojects
        .filter { it.buildFile.exists() && it.path != project.path && it.path != ":core:testing" }
        .forEach { add("kover", project(it.path)) }

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

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Unit testing
    testImplementation(project(":core:testing"))
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
