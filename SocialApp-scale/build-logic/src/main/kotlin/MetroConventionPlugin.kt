import org.gradle.api.Plugin
import org.gradle.api.Project

/** Applies the Metro compiler plugin so a module can contribute to the DI graph. */
class MetroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("dev.zacsweers.metro")
    }
}
