package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatProcessorListener
import java.nio.file.Path

/**
 * Arguments for [DiktatRunner]
 *
 * @property files a collection of files which needs to be fixed
 * @property loggingListener listener to log diktat runner phases, [DiktatProcessorListener.empty] by default
 */
data class DiktatRunnerArguments(
    val files: Collection<Path>,
    val loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
)
