package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import java.io.InputStream
import java.nio.file.Path

/**
 * Arguments for [DiktatRunner]
 *
 * @property configInputStream an input stream with config to load Diktat's rules or null to use default configs
 * @property sourceRootDir a common root dir for all provided [files]
 * @property files a collection of files which needs to be fixed
 * @property baselineFile an optional path to file with baseline
 * @property reporterArgsList list of arguments to create reporters to report result
 * @property loggingListener listener to log diktat runner phases, [DiktatProcessorListener.empty] by default
 */
data class DiktatRunnerArguments(
    val configInputStream: InputStream?,
    val sourceRootDir: Path?,
    val files: Collection<Path>,
    val baselineFile: Path?,
    val reporterArgsList: List<DiktatReporterCreationArguments> = emptyList(),
    val loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
)
