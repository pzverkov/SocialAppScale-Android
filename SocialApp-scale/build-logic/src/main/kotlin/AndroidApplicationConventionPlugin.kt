import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Convention applied by the app module and reused by future Android application
 * targets. Encapsulates the cross-cutting Android + Kotlin baseline (SDK levels,
 * Java/Kotlin 17, Compose enablement) so module build files stay declarative.
 * App-specific config (applicationId, signing, build types, lint, Kover) stays
 * in the module that owns it.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.application")
            apply("org.jetbrains.kotlin.plugin.compose")
            apply("org.jetbrains.kotlinx.kover")
        }

        extensions.configure<ApplicationExtension> {
            // compileSdk runs ahead of targetSdk: it lets newer-API libraries (androidx.core 1.19+)
            // compile, while targetSdk 36 keeps runtime behavior unchanged.
            compileSdk = 37
            defaultConfig {
                minSdk = 26
                targetSdk = 36
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                compose = true
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(17)
        }
    }
}
