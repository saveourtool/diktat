package org.cqfn.diktat

import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Arguments for [DiktatRunner]
 *
 * @property configFileName a config file to load Diktat's rules
 * @property sourceRootDir a common root dir for all provided [files]
 * @property files a collection of files which needs to be fixed
 * @property baselineFile an optional path to file with baseline
 * @property reporterType type of reporter to report the detected errors
 * @property reporterOutput output for reporter
 */
data class DiktatRunnerArguments(
    val configFileName: String,
    val sourceRootDir: Path,
    val files: Collection<Path>,
    val baselineFile: Path?,
    val reporterType: String,
    val reporterOutput: OutputStream?,
) {
    constructor(
        configFile: Path,
        sourceRootDir: Path,
        files: Collection<Path>,
        baselineFile: Path?,
        reporterType: String,
        reporterOutput: OutputStream?,
    ) : this(
        configFile.absolutePathString(),
        sourceRootDir,
        files,
        baselineFile,
        reporterType,
        reporterOutput,
    )
}
