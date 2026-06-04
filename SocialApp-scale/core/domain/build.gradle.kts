plugins {
    id("socialapp.jvm.library")
}

dependencies {
    // Item and ErrorType appear in the repository signatures, so expose them.
    api(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
}
