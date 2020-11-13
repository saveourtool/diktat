package org.cqfn.diktat.plugin.gradle

import com.pinterest.ktlint.reporter.plain.PlainReporter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

/**
 * Plugin that configures diktat and registers tasks to run diktat
 */
@Suppress("unused", "MagicNumber")
class DiktatGradlePlugin : Plugin<Project> {
    /**
     * @param project a gradle [Project] that the plugin is applied to
     */
    override fun apply(project: Project) {
        val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java)
        diktatExtension.inputs = project.fileTree("src").apply {
            include("**/*.kt")
        }
        diktatExtension.reporter = PlainReporter(System.out)

        // only gradle 7+ (or maybe 6.8) will embed kotlin 1.4+, kx.serialization is incompatible with kotlin 1.3, so until then we have to use JavaExec wrapper
        // FixMe: when gradle with kotlin 1.4 is out, proper configurable tasks should be added
        // configuration to provide JavaExec with correct classpath
        val diktatConfiguration = project.configurations.create(DIKTAT_CONFIGURATION) { configuration ->
            configuration.isVisible = false
            configuration.dependencies.add(project.dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:1.4.10"))
            configuration.dependencies.add(project.dependencies.create("com.pinterest:ktlint:0.39.0", closureOf<ExternalModuleDependency> {
                exclude(
                    mutableMapOf(
                           "group" to "com.pinterest.ktlint",
                          "module" to "ktlint-ruleset-standard"
                    )
                )
            }))
            configuration.dependencies.add(project.dependencies.create("org.cqfn.diktat:diktat-rules:0.1.4-SNAPSHOT"))
        }

        project.registerDiktatCheckTask(diktatExtension, diktatConfiguration)
        project.registerDiktatFixTask(diktatExtension, diktatConfiguration)
    }

    companion object {
        const val DIKTAT_CHECK_TASK = "diktatCheck"
        const val DIKTAT_CONFIGURATION = "diktat"
        const val DIKTAT_EXTENSION = "diktat"
        const val DIKTAT_FIX_TASK = "diktatFix"
    }
}
