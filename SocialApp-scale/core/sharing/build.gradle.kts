plugins {
    id("socialapp.android.library")
    id("socialapp.android.metro")
}

android {
    namespace = "com.pzverkov.socialapp.core.sharing"
}

dependencies {
    testImplementation(libs.junit)
}
