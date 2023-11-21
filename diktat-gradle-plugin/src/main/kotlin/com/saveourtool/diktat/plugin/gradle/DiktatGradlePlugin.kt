package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.plugin.gradle.tasks.DiktatCheckTask.Companion.registerDiktatCheckTask
import com.saveourtool.diktat.plugin.gradle.tasks.DiktatFixTask.Companion.registerDiktatFixTask
import com.saveourtool.diktat.plugin.gradle.tasks.configureMergeReportsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet

/**
 * Plugin that configures diktat and registers tasks to run diktat
 */
class DiktatGradlePlugin : Plugin<Project> {
    /**
     * @param project a gradle [Project] that the plugin is applied to
     */
    override fun apply(project: Project) {
        val patternSet = PatternSet()
        val diktatExtension = project.extensions.create(
            DIKTAT_EXTENSION,
            DiktatExtension::class.java,
            patternSet,
        ).apply {
            diktatConfigFile = project.rootProject.file("diktat-analysis.yml")
        }

        project.registerDiktatCheckTask(diktatExtension, patternSet)
        project.registerDiktatFixTask(diktatExtension, patternSet)
        project.configureMergeReportsTask()
    }

    companion object {
        /**
         * Task to check diKTat
         */
        const val DIKTAT_CHECK_TASK = "diktatCheck"

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
    }
}
