package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatProcessorListener
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * Arguments for [DiktatRunner]
 *
 * @property configInputStream an input stream with config to load Diktat's rules
 * @property sourceRootDir a common root dir for all provided [files]
 * @property files a collection of files which needs to be fixed
 * @property baselineFile an optional path to file with baseline
 * @property reporterType type of reporter to report the detected errors
 * @property reporterOutput output for reporter
 * @property groupByFileInPlain a flag `groupByFile` which is applicable for plain reporter only, **null** by default
 * @property colorNameInPlain a color name  which is applicable for plain reporter only, **null** by default
 * @property loggingListener listener to log diktat runner phases, [DiktatProcessorListener.empty] by default
 */
data class DiktatRunnerArguments(
    val configInputStream: InputStream,
    val sourceRootDir: Path?,
    val files: Collection<Path>,
    val baselineFile: Path?,
    val reporterType: String,
    val reporterOutput: OutputStream?,
    val groupByFileInPlain: Boolean? = null,
    val colorNameInPlain: String? = null,
    val loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
) {
    constructor(
        configFile: Path,
        sourceRootDir: Path?,
        files: Collection<Path>,
        baselineFile: Path?,
        reporterType: String,
        reporterOutput: OutputStream?,
        groupByFileInPlain: Boolean? = null,
        colorNameInPlain: String? = null,
        loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
    ) : this(
        configFile.inputStream(),
        sourceRootDir,
        files,
        baselineFile,
        reporterType,
        reporterOutput,
        groupByFileInPlain,
        colorNameInPlain,
        loggingListener,
    )
}
