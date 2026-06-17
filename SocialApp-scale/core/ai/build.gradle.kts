plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
}

android {
    namespace = "com.pzverkov.socialapp.core.ai"
}

dependencies {
    // On-device Gemini Nano via ML Kit GenAI. Present on capable devices only; the client
    // reports UNAVAILABLE everywhere else and never throws, so callers degrade gracefully.
    implementation(libs.mlkit.genai.summarization)
    implementation(libs.mlkit.genai.image.description)
    // On-device translation + language identification (classic ML Kit, broad device support).
    implementation(libs.mlkit.translate)
    implementation(libs.mlkit.language.id)
    // ML Kit GenAI returns Guava ListenableFuture; classic ML Kit returns GMS Tasks.
    // Both await() bridges are needed.
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.core)
}
