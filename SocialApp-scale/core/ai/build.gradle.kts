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
    // ML Kit GenAI returns Guava ListenableFuture; this adds the coroutine await() bridge.
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.kotlinx.coroutines.core)
}
