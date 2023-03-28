package org.cqfn.diktat.plugin.gradle.tasks

import org.cqfn.diktat.DiktatProcessCommand
import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import javax.inject.Inject

/**
 * A task to check source code by diktat
 */
abstract class DiktatCheckTask @Inject constructor(
    extension: DiktatExtension,
    inputs: PatternFilterable
) : DiktatTaskBase(extension, inputs) {
    override fun doRun(diktatCommand: DiktatProcessCommand, formattedContentConsumer: (String) -> Unit) {
        diktatCommand.check()
    }

    companion object {
        /**
         * @param diktatExtension [DiktatExtension] with some values for task configuration
         * @param patternSet [PatternSet] to discover files for diktat check
         * @return a [TaskProvider]
         */
        fun Project.registerDiktatCheckTask(
            diktatExtension: DiktatExtension,
            patternSet: PatternSet
        ): TaskProvider<DiktatCheckTask> =
                tasks.register(
                    DiktatGradlePlugin.DIKTAT_CHECK_TASK, DiktatCheckTask::class.java,
                    diktatExtension, patternSet
                )
    }
}
