plugins {
    id("socialapp.jvm.library")
}

dependencies {
    // Shared test doubles implement the domain contracts.
    api(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.core)
}
