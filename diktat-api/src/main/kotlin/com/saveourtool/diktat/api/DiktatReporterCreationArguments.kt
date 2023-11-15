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
     * Identifier of [DiktatReporter] which needs to be created
     */
    val id: String

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
         * @param id ID of [DiktatReporter]
         * @param outputStream stdout will be used when it's empty
         * @param sourceRootDir a dir to detect relative path for processing files
         * @param colorNameInPlain a color name for colorful output which is applicable for plain ([DiktatReporterFactory.PLAIN_ID]) reporter only,
         * `null` means to disable colorization.
         * @param groupByFileInPlain a flag `groupByFile` which is applicable for plain ([DiktatReporterFactory.PLAIN_ID]) reporter only.
         * @return created [DiktatReporter]
         */
        operator fun invoke(
            id: String,
            outputStream: OutputStream?,
            sourceRootDir: Path?,
            colorNameInPlain: String? = null,
            groupByFileInPlain: Boolean? = null,
        ): DiktatReporterCreationArguments {
            val (outputStreamOrStdout, closeOutputStreamAfterAll) = outputStream?.let { it to true } ?: (System.`out` to false)
            return if (id == DiktatReporterFactory.PLAIN_ID) {
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
                    id, outputStreamOrStdout, closeOutputStreamAfterAll, sourceRootDir
                )
            }
        }
    }
}

/**
 * Implementation of [DiktatReporterCreationArguments] for [DiktatReporterFactory.PLAIN_ID]
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
    override val id: String = DiktatReporterFactory.PLAIN_ID
}

/**
 * @property id
 * @property outputStream
 * @property closeOutputStreamAfterAll
 * @property sourceRootDir
 */
private data class DiktatReporterCreationArgumentsImpl(
    override val id: String,
    override val outputStream: OutputStream,
    override val closeOutputStreamAfterAll: Boolean,
    override val sourceRootDir: Path?,
) : DiktatReporterCreationArguments
