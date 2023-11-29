package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * Arguments for [DiktatRunnerFactory] to create [DiktatRunner]
 *
 * @property configInputStream an input stream with config to load Diktat's rules
 * @property sourceRootDir a common root dir for all provided [DiktatRunnerArguments.files]
 * @property baselineFile an optional path to file with baseline
 * @property reporterArgsList list of arguments to create reporters to report result
 */
data class DiktatRunnerFactoryArguments(
    val configInputStream: InputStream,
    val sourceRootDir: Path?,
    val baselineFile: Path?,
    val reporterArgsList: List<DiktatReporterCreationArguments> = emptyList(),
) {
    constructor(
        configFile: Path,
        sourceRootDir: Path?,
        baselineFile: Path?,
        reporterArgsList: List<DiktatReporterCreationArguments> = emptyList(),
    ) : this(
        configFile.inputStream(),
        sourceRootDir,
        baselineFile,
        reporterArgsList,
    )
}
