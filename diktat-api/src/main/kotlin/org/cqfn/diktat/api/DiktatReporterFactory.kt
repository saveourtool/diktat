package org.cqfn.diktat.api

import java.io.OutputStream
import java.nio.file.Path

typealias DiktatReporter = DiktatProcessorListener

/**
 * A factory to create [DiktatReporter]
 */
interface DiktatReporterFactory : Function3<String, OutputStream, Path, DiktatReporter> {
    /**
     * Set of supported IDs
     */
    val ids: Set<String>

    /**
     * ID of [DiktatReporter] for plain output
     */
    val plainId: String

    /**
     * Names of color for plain output
     */
    val colorNamesInPlain: Set<String>

    /**
     * @param id ID of [DiktatReporter]
     * @param outputStream
     * @param sourceRootDir a dir to detect relative path for processing files
     * @return created [DiktatReporter]
     */
    override operator fun invoke(
        id: String,
        outputStream: OutputStream,
        sourceRootDir: Path,
    ): DiktatReporter

    /**
     * @param outputStream
     * @param sourceRootDir a dir to detect relative path for processing files
     * @param colorName name of color for colorful output, `null` means to disable colorization.
     * @param groupByFile
     * @return [DiktatReporter] for plain output
     */
    fun createPlain(
        outputStream: OutputStream,
        sourceRootDir: Path,
        colorName: String? = null,
        groupByFile: Boolean? = null,
    ): DiktatReporter
}
