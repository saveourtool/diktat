package org.cqfn.diktat.test.framework.processing

import com.github.difflib.DiffUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.StringJoiner
import java.util.stream.Collectors

class FileComparator {
    private val expectedResultFile: File
    private val actualResultList: List<String?>

    constructor(expectedResultFile: File, actualResultList: List<String?>) {
        this.expectedResultFile = expectedResultFile
        this.actualResultList = actualResultList
    }

    constructor (expectedResultFile: File, actualResultFile: File) {
        this.expectedResultFile = expectedResultFile
        this.actualResultList = readFile(actualResultFile.absolutePath)
    }

    /**
     * @return true in case files are different
     * false - in case they are equals
     */
    @Suppress("ReturnCount", "FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesEqual(): Boolean {
        try {
            val expect = readFile(expectedResultFile.absolutePath)
            if (expect.isEmpty()) {
                return false
            }
            val patch = DiffUtils.diff(expect, actualResultList)
            if (patch.deltas.isEmpty()) {
                return true
            }
            val deltasJoiner = StringJoiner(System.lineSeparator())
            patch
                    .deltas
                    .map { it.toString() }
                    .forEach { delta -> deltasJoiner.add(delta) }

            log.error("""Expected result from <${expectedResultFile.name}> and actual formatted are different.
|                        See difference below:
|                           Expected  vs  Actual ${System.lineSeparator()}$deltasJoiner""".trimMargin())

        } catch (e: RuntimeException) {
            log.error("Not able to prepare diffs between <${expectedResultFile.name}> and <${actualResultList}>", e)
        }
        return false
    }

    /**
     * @param fileName - file where to write these list to, separated with newlines
     */
    fun readFile(fileName: String): List<String> {
        var list: List<String> = ArrayList()
        try {
            Files.newBufferedReader(Paths.get(fileName)).use { list = it.lines().collect(Collectors.toList()) }
        } catch (e: IOException) {
            log.error("Not able to read file: $fileName")
        }
        return list
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileComparator::class.java)
    }
}
