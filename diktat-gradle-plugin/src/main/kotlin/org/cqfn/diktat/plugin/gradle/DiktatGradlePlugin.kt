package org.cqfn.diktat.plugin.gradle

import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.util.PatternSet

/**
 * Plugin that configures diktat and registers tasks to run diktat
 */
@Suppress("unused", "MagicNumber")
class DiktatGradlePlugin : Plugin<Project> {
    /**
     * @param project a gradle [Project] that the plugin is applied to
     */
    override fun apply(project: Project) {
        val patternSet = PatternSet()
        val diktatExtension = project.extensions.create(
            DIKTAT_EXTENSION,
            DiktatExtension::class.java,
            patternSet
        ).apply {
            diktatConfigFile = project.rootProject.file("diktat-analysis.yml")
        }

        // only gradle 7+ (or maybe 6.8) will embed kotlin 1.4+, kx.serialization is incompatible with kotlin 1.3, so until then we have to use JavaExec wrapper
        // FixMe: when gradle with kotlin 1.4 is out, proper configurable tasks should be added
        // configuration to provide JavaExec with correct classpath
        val diktatConfiguration = project.configurations.create(DIKTAT_CONFIGURATION) { configuration ->
            configuration.isVisible = false
            configuration.dependencies.add(project.dependencies.create("com.pinterest:ktlint:$KTLINT_VERSION", closureOf<ExternalModuleDependency> {
                exclude(
                    mutableMapOf(
                        "group" to "com.pinterest.ktlint",
                        "module" to "ktlint-ruleset-standard"
                    )
                )
                attributes {
                    it.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling::class.java, Bundling.EXTERNAL))
                }
            }))
            configuration.dependencies.add(project.dependencies.create("org.cqfn.diktat:diktat-rules:$DIKTAT_VERSION"))
        }

        project.registerDiktatCheckTask(diktatExtension, diktatConfiguration, patternSet)
        project.registerDiktatFixTask(diktatExtension, diktatConfiguration, patternSet)
    }

    companion object {
        /**
         * Task to check diKTat
         */
        const val DIKTAT_CHECK_TASK = "diktatCheck"

        /**
         * DiKTat configuration
         */
        const val DIKTAT_CONFIGURATION = "diktat"

        /**
         * DiKTat extension
         */
        const val DIKTAT_EXTENSION = "diktat"

        /**
         * Task to run diKTat with fix
         */
        const val DIKTAT_FIX_TASK = "diktatFix"
    }
}
