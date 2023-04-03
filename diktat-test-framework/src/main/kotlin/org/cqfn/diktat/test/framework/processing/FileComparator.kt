package org.cqfn.diktat.test.framework.processing

import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.ChangeDelta
import io.github.petertrr.diffutils.text.DiffRowGenerator
import mu.KotlinLogging

import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.readLines

/**
 * A class that is capable of comparing files content
 */
class FileComparator(
    private val expectedResultFileName: String,
    private val expectedResultList: List<String>,
    private val actualResultFileName: String,
    private val actualResultList: List<String>,
) {
    private val diffGenerator = DiffRowGenerator(
        columnWidth = Int.MAX_VALUE,
        showInlineDiffs = true,
        mergeOriginalRevised = false,
        inlineDiffByWord = false,
        oldTag = { _, start -> if (start) "[" else "]" },
        newTag = { _, start -> if (start) "<" else ">" },
    )

    /**
     * delta in files
     */
    val delta: String? by lazy {
        if (expectedResultList.isEmpty()) {
            return@lazy null
        }
        val regex = (".*// ;warn:?(.*):(\\d*): (.+)").toRegex()
        val expectWithoutWarn = expectedResultList.filterNot { line ->
            line.contains(regex)
        }
        val patch = diff(expectWithoutWarn, actualResultList)

        if (patch.deltas.isEmpty()) {
            return@lazy null
        }
        return@lazy patch.deltas.joinToString(System.lineSeparator()) { delta ->
            when (delta) {
                is ChangeDelta -> diffGenerator
                    .generateDiffRows(delta.source.lines, delta.target.lines)
                    .joinToString(prefix = "ChangeDelta, position ${delta.source.position}, lines:\n", separator = "\n\n") {
                        """
                            |-${it.oldLine}
                            |+${it.newLine}
                            |""".trimMargin()
                    }
                    .let { "ChangeDelta, position ${delta.source.position}, lines:\n$it" }
                else -> delta.toString()
            }
        }
    }

    constructor(
        expectedResultFile: File,
        actualResultList: List<String>
    ) : this(
        expectedResultFileName = expectedResultFile.name,
        expectedResultList = readFile(expectedResultFile.toPath()),
        actualResultFileName = "No file name.kt",
        actualResultList = actualResultList,
    )

    constructor(
        expectedResultFile: File,
        actualResultFile: File
    ) : this(
        expectedResultFileName = expectedResultFile.name,
        expectedResultList = readFile(expectedResultFile.toPath()),
        actualResultFileName = actualResultFile.name,
        actualResultList = readFile(actualResultFile.toPath()),
    )

    /**
     * @return true in case files are different
     * false - in case they are equals
     */
    @Suppress(
        "ReturnCount",
        "FUNCTION_BOOLEAN_PREFIX",
        "TOO_LONG_FUNCTION"
    )
    fun compareFilesEqual(): Boolean {
        try {
            if (expectedResultList.isEmpty()) {
                return false
            }
            val joinedDeltas = delta ?: return true
            log.error("""
                |Expected result from <$expectedResultFileName> and <$actualResultFileName> formatted are different.
                |See difference below:
                |$joinedDeltas
                """.trimMargin()
            )
            return false
        } catch (e: IllegalArgumentException) {
            log.error("Not able to prepare diffs between <$expectedResultFileName> and <$actualResultFileName>", e)
            return false
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}

        /**
         * @param file file where to write these list to, separated with newlines.
         * @return a list of lines from the file, or an empty list if an I/O error
         *   has occurred.
         */
        private fun readFile(file: Path): List<String> =
            try {
                file.readLines()
            } catch (e: IOException) {
                log.error("Not able to read file: $file")
                emptyList()
            }
    }
}
