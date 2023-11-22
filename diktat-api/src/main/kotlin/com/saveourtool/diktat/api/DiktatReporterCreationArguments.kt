/**
 * Contains a base interface and implementations for a container with arguments to create a reporter
 */

package com.saveourtool.diktat.api

import java.io.OutputStream
import java.nio.file.Path

/**
 * Arguments to create [DiktatReporter] using [DiktatReporterFactory]
 */
sealed interface DiktatReporterCreationArguments {
    /**
     * Type of [DiktatReporter] which needs to be created
     */
    val reporterType: DiktatReporterType

    /**
     * Output for [DiktatReporter]
     */
    val outputStream: OutputStream

    /**
     * Should [outputStream] be closed af the end of [DiktatReporter]
     */
    val closeOutputStreamAfterAll: Boolean

    /**
     * Directory to base source root to report relative paths in [DiktatReporter]
     */
    val sourceRootDir: Path?

    companion object {
        /**
         * @param reporterType type of [DiktatReporter]
         * @param outputStream stdout will be used when it's empty
         * @param sourceRootDir a dir to detect relative path for processing files
         * @param colorNameInPlain a color name for colorful output which is applicable for plain ([DiktatReporterType.PLAIN]) reporter only,
         * `null` means to disable colorization.
         * @param groupByFileInPlain a flag `groupByFile` which is applicable for plain ([DiktatReporterType.PLAIN]) reporter only.
         * @return created [DiktatReporter]
         */
        operator fun invoke(
            reporterType: DiktatReporterType,
            outputStream: OutputStream?,
            sourceRootDir: Path?,
            colorNameInPlain: String? = null,
            groupByFileInPlain: Boolean? = null,
        ): DiktatReporterCreationArguments {
            val (outputStreamOrStdout, closeOutputStreamAfterAll) = outputStream?.let { it to true } ?: (System.`out` to false)
            return if (reporterType == DiktatReporterType.PLAIN) {
                PlainDiktatReporterCreationArguments(
                    outputStreamOrStdout, closeOutputStreamAfterAll, sourceRootDir, colorNameInPlain, groupByFileInPlain
                )
            } else {
                require(colorNameInPlain == null) {
                    "colorization is applicable only for plain reporter"
                }
                require(groupByFileInPlain == null) {
                    "groupByFile is applicable only for plain reporter"
                }
                DiktatReporterCreationArgumentsImpl(
                    reporterType, outputStreamOrStdout, closeOutputStreamAfterAll, sourceRootDir
                )
            }
        }
    }
}

/**
 * Implementation of [DiktatReporterCreationArguments] for [DiktatReporterType.PLAIN]
 *
 * @property outputStream
 * @property closeOutputStreamAfterAll
 * @property sourceRootDir
 * @property colorName name of color for colorful output, `null` means to disable colorization.
 * @property groupByFile
 */
data class PlainDiktatReporterCreationArguments(
    override val outputStream: OutputStream,
    override val closeOutputStreamAfterAll: Boolean,
    override val sourceRootDir: Path?,
    val colorName: String? = null,
    val groupByFile: Boolean? = null,
) : DiktatReporterCreationArguments {
    override val reporterType: DiktatReporterType = DiktatReporterType.PLAIN
}

/**
 * @property reporterType
 * @property outputStream
 * @property closeOutputStreamAfterAll
 * @property sourceRootDir
 */
private data class DiktatReporterCreationArgumentsImpl(
    override val reporterType: DiktatReporterType,
    override val outputStream: OutputStream,
    override val closeOutputStreamAfterAll: Boolean,
    override val sourceRootDir: Path?,
) : DiktatReporterCreationArguments
