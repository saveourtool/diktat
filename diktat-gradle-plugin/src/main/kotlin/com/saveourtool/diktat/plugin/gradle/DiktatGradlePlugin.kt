package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.plugin.gradle.extensions.Reporter
import com.saveourtool.diktat.plugin.gradle.extensions.Reporters
import com.saveourtool.diktat.plugin.gradle.tasks.DiktatCheckTask.Companion.registerDiktatCheckTask
import com.saveourtool.diktat.plugin.gradle.tasks.DiktatFixTask.Companion.registerDiktatFixTask
import com.saveourtool.diktat.plugin.gradle.tasks.configureMergeReportsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet

/**
 * Plugin that configures diktat and registers tasks to run diktat
 */
@Suppress("unused", "MagicNumber")
class DiktatGradlePlugin : Plugin<Project> {
    /**
     * @param project a gradle [Project] that the plugin is applied to
     */
    @Suppress("TOO_LONG_FUNCTION")
    override fun apply(project: Project) {

        val patternSet = PatternSet()
        val reporters = mutableListOf<Reporter>()
        val diktatExtension = project.extensions.create(
            DIKTAT_EXTENSION,
            DiktatExtension::class.java,
            patternSet,
            project.objects.newInstance(Reporters::class.java, reporters),
        ).apply {
            diktatConfigFile = project.rootProject.file("diktat-analysis.yml")
        }

        project.registerDiktatCheckTask(diktatExtension, patternSet, reporters)
        project.registerDiktatFixTask(diktatExtension, patternSet, reporters)
        project.configureMergeReportsTask(diktatExtension)
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

        /**
         * Name of the task that merges SARIF reports of diktat tasks
         */
        internal const val MERGE_SARIF_REPORTS_TASK_NAME = "mergeDiktatReports"

        /**
         * Version of JVM with more strict module system, which requires `add-opens` for kotlin compiler
         */
        const val MIN_JVM_REQUIRES_ADD_OPENS = 16
    }
}
