package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.util.readTextOrNull

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.ChangeDelta
import io.github.petertrr.diffutils.text.DiffRowGenerator

import java.io.File

/**
 * A class that is capable of comparing files content
 *
 * @property fileName
 * @property expectedResult
 * @property actualResult
 */
class FileComparator(
    private val fileName: String,
    private val expectedResult: String,
    private val actualResult: String,
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
     * expected result without lines with warns
     */
    val expectedResultWithoutWarns: String by lazy {
        val regex = (".*// ;warn:?(.*):(\\d*): (.+)").toRegex()
        expectedResult
            .split("\n")
            .filterNot { line ->
                line.contains(regex)
            }
            .joinToString("\n")
    }

    /**
     * delta in files
     */
    val delta: String? by lazy {
        if (expectedResult.isEmpty()) {
            return@lazy null
        }
        val patch = diff(expectedResultWithoutWarns, actualResult, null)

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
        actualResult: String
    ) : this(
        fileName = expectedResultFile.name,
        expectedResult = expectedResultFile.toPath().readTextOrNull().orEmpty(),
        actualResult = actualResult,
    )

    constructor(
        expectedResultFile: File,
        actualResultFile: File
    ) : this(
        fileName = expectedResultFile.name,
        expectedResult = expectedResultFile.toPath().readTextOrNull().orEmpty(),
        actualResult = actualResultFile.toPath().readTextOrNull().orEmpty(),
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
            if (expectedResult.isEmpty()) {
                return false
            }
            val joinedDeltas = delta ?: return true
            log.error {
                """
                    |Expected result for <$fileName> formatted are different.
                    |See difference below:
                    |$joinedDeltas
                """.trimMargin()
            }
            return false
        } catch (e: IllegalArgumentException) {
            log.error(e) { "Not able to prepare diffs for <$fileName>" }
            return false
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
