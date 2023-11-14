package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/**
 * A task to check source code by diktat
 */
abstract class DiktatFixTask @Inject constructor(
    extension: DiktatExtension,
) : DiktatTaskBase(extension) {
    override fun doRun(
        runner: DiktatRunner,
        args: DiktatRunnerArguments
    ): Int = runner.fixAll(args) { updatedFile ->
        project.logger.info("Original and formatted content differ, writing to ${updatedFile.fileName}...")
    }

    companion object {
        /**
         * @param diktatExtension [DiktatExtension] with some values for task configuration
         * @return a [TaskProvider]
         */
        fun Project.registerDiktatFixTask(
            diktatExtension: DiktatExtension,
        ): TaskProvider<DiktatFixTask> =
            tasks.register(
                DiktatGradlePlugin.DIKTAT_FIX_TASK, DiktatFixTask::class.java,
                diktatExtension,
            )
    }
}
