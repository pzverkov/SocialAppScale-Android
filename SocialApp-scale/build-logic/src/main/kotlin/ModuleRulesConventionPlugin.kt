import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Enforces the module dependency invariant the whole architecture rests on:
 * no feature depends on another feature, and no core module depends on a feature.
 * :app is exempt - it is the composition root and depends on every feature.
 *
 * The check runs at configuration time over the module's own declared project
 * dependencies (no resolution, no cross-project state held into execution), so it
 * stays config-cache clean.
 */
class ModuleRulesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val isFeature = path.startsWith(":feature:")
        val isCore = path.startsWith(":core:")
        if (!isFeature && !isCore) return@with

        afterEvaluate {
            val projectDeps = listOf("implementation", "api")
                .mapNotNull { configurations.findByName(it) }
                .flatMap { it.dependencies.withType(ProjectDependency::class.java) }
                .map { it.path }

            projectDeps.forEach { depPath ->
                if (isFeature && depPath != path && depPath.startsWith(":feature:")) {
                    throw GradleException(
                        "Module rule: $path must not depend on another feature ($depPath). " +
                            "Features stay independent; share code through :core modules.",
                    )
                }
                if (isCore && depPath.startsWith(":feature:")) {
                    throw GradleException(
                        "Module rule: $path (core) must not depend on a feature ($depPath). " +
                            "Core layers may not depend on features.",
                    )
                }
            }
        }
    }
}
