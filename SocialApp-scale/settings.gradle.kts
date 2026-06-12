pluginManagement {
    // Convention plugins (socialapp.android.*) live in a composite build so module
    // build files apply shared config by id instead of repeating it.
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Resolves and auto-provisions JVM toolchains, including the daemon JVM pinned
// in gradle/gradle-daemon-jvm.properties, so the build runs on the same JDK
// across CLI, IDE, and CI without relying on a local JAVA_HOME.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Plugin and dependency versions live in the shared monorepo catalog at the
    // workspace root, so a bump applies to every project at once.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "socialapp-scale"
include(":app")
include(":core:model")
include(":core:domain")
include(":core:common")
include(":core:network")
include(":core:ui")
include(":core:sharing")
include(":core:testing")
include(":core:navigation")
include(":feature:itemlist")
include(":feature:itemdetail")
include(":feature:favorite")
