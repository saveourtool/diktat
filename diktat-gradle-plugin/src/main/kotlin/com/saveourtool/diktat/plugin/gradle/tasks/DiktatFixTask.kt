package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin
import com.saveourtool.diktat.plugin.gradle.extensions.Reporter
import com.saveourtool.diktat.plugin.gradle.extensions.Reporters
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import javax.inject.Inject

/**
 * A task to check source code by diktat
 */
abstract class DiktatFixTask @Inject constructor(
    extension: DiktatExtension,
    inputs: PatternFilterable,
    reporters: List<Reporter>,
    objectFactory: ObjectFactory,
) : DiktatTaskBase(extension, inputs, objectFactory) {
    override fun doRun(
        runner: DiktatRunner,
        args: DiktatRunnerArguments
    ): Int = runner.fixAll(args) { updatedFile ->
        project.logger.info("Original and formatted content differ, writing to ${updatedFile.fileName}...")
    }

    companion object {
        /**
         * @param diktatExtension [DiktatExtension] with some values for task configuration
         * @param patternSet [PatternSet] to discover files for diktat fix
         * @param reporters [List] of [Reporter] to configure reporters for diktat fix
         * @return a [TaskProvider]
         */
        fun Project.registerDiktatFixTask(
            diktatExtension: DiktatExtension,
            patternSet: PatternSet,
            reporters: List<Reporter>,
        ): TaskProvider<DiktatFixTask> =
            tasks.register(
                DiktatGradlePlugin.DIKTAT_FIX_TASK, DiktatFixTask::class.java,
                diktatExtension, patternSet, reporters,
            )
    }
}
