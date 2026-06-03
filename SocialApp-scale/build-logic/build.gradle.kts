plugins {
    `kotlin-dsl`
}

// AGP and the Kotlin Gradle plugin are needed only to compile against their DSL
// types (ApplicationExtension, KotlinAndroidProjectExtension). At runtime the
// consuming build supplies the actual plugin versions from the root classpath.
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "socialapp.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
