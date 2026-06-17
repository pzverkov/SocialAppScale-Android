dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // SocialApp-scale's own catalog (../gradle), shared with the scale build so convention
    // plugins resolve the same versions as the modules. The frozen SocialApp-basic keeps the
    // monorepo-root catalog.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
