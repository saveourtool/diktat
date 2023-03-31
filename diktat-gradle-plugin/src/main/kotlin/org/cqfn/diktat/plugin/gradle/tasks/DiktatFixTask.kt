package org.cqfn.diktat.plugin.gradle.tasks

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import java.nio.file.Path
import javax.inject.Inject

/**
 * A task to check source code by diktat
 */
abstract class DiktatFixTask @Inject constructor(
    extension: DiktatExtension,
    inputs: PatternFilterable
) : DiktatTaskBase(extension, inputs) {
    override fun doRun(
        diktatProcessor: DiktatProcessor,
        listener: DiktatProcessorListener,
        files: Sequence<Path>, formattedContentConsumer: (Path, String) -> Unit
    ) {
        diktatProcessor.fixAll(listener, files, formattedContentConsumer)
    }

    companion object {
        /**
         * @param diktatExtension [DiktatExtension] with some values for task configuration
         * @param patternSet [PatternSet] to discover files for diktat fix
         * @return a [TaskProvider]
         */
        fun Project.registerDiktatFixTask(
            diktatExtension: DiktatExtension,
            patternSet: PatternSet
        ): TaskProvider<DiktatFixTask> =
            tasks.register(
                DiktatGradlePlugin.DIKTAT_FIX_TASK, DiktatFixTask::class.java,
                diktatExtension, patternSet
            )
    }
}
