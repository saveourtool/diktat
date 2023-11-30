package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import javax.inject.Inject

/**
 * A task to check source code by diktat
 */
abstract class DiktatCheckTask @Inject constructor(
    extension: DiktatExtension,
    inputs: PatternFilterable,
    objectFactory: ObjectFactory,
) : DiktatTaskBase(
    extension,
    inputs,
    objectFactory
) {
    override fun doRun(
        runner: DiktatRunner,
        args: DiktatRunnerArguments
    ): Int = runner.checkAll(args)

    companion object {
        /**
         * @param diktatExtension [DiktatExtension] with some values for task configuration
         * @param patternSet [PatternSet] to discover files for diktat check
         * @return a [TaskProvider]
         */
        fun Project.registerDiktatCheckTask(
            diktatExtension: DiktatExtension,
            patternSet: PatternSet,
        ): TaskProvider<DiktatCheckTask> =
            tasks.register(
                DiktatGradlePlugin.DIKTAT_CHECK_TASK, DiktatCheckTask::class.java,
                diktatExtension, patternSet,
            ).also { it.configure(diktatExtension) }
    }
}
