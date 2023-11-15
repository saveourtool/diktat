package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporterArguments
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * Arguments for [DiktatRunner]
 *
 * @property configInputStream an input stream with config to load Diktat's rules
 * @property sourceRootDir a common root dir for all provided [files]
 * @property files a collection of files which needs to be fixed
 * @property baselineFile an optional path to file with baseline
 * @property reporterArgsList list of arguments to create reporters to report result
 * @property loggingListener listener to log diktat runner phases, [DiktatProcessorListener.empty] by default
 */
data class DiktatRunnerArguments(
    val configInputStream: InputStream,
    val sourceRootDir: Path?,
    val files: Collection<Path>,
    val baselineFile: Path?,
    val reporterArgsList: List<DiktatReporterArguments> = emptyList(),
    val loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
) {
    constructor(
        configFile: Path,
        sourceRootDir: Path?,
        files: Collection<Path>,
        baselineFile: Path?,
        reporterArgsList: List<DiktatReporterArguments> = emptyList(),
        loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
    ) : this(
        configFile.inputStream(),
        sourceRootDir,
        files,
        baselineFile,
        reporterArgsList,
        loggingListener,
    )
}
