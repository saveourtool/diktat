package org.cqfn.diktat.test.framework.processing

import mu.KotlinLogging
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readLines

/**
 * Class that can apply transformation to an input file and then compare with expected result and output difference.
 *
 * @property resourceFilePath only used when the files are loaded as resources,
 *   via [compareFilesFromResources].
 * @property function a transformation that will be applied to the file
 */
@Suppress("ForbiddenComment", "TYPE_ALIAS")
class TestComparatorUnit(
    private val resourceFilePath: String,
    private val function: (expectedText: String, testFilePath: String) -> String,
) {
    /**
     * @param expectedResult the name of the resource which has the expected
     *   content. The trailing newline, if any, **won't be read** as a separate
     *   empty string. So, if the content to be read from this file is expected
     *   to be terminated with an empty string (which is the case if
     *   `newlineAtEnd` is `true`), then the file should end with **two**
     *   consecutive linebreaks.
     * @param testFileStr the name of the resource which has the original content.
     * @param trimLastEmptyLine whether the last (empty) line should be
     *   discarded when reading the content of [testFileStr].
     * @param replacements a map of replacements which will be applied to [expectedResult] and [testFileStr] before comparing.
     * @return the result of file comparison by their content.
     * @see compareFilesFromFileSystem
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromResources(
        expectedResult: String,
        testFileStr: String,
        trimLastEmptyLine: Boolean = false,
        replacements: Map<String, String> = emptyMap(),
    ): FileComparisonResult {
        val expectedPath = javaClass.classLoader.getResource("$resourceFilePath/$expectedResult")
        val testPath = javaClass.classLoader.getResource("$resourceFilePath/$testFileStr")
        if (testPath == null || expectedPath == null) {
            log.error("Not able to find files for running test: $expectedResult and $testFileStr")
            return FileComparisonResult(
                isSuccessful = false,
                delta = null,
                actualContent = "// $resourceFilePath/$expectedResult is found: ${testPath != null}",
                expectedContent = "// $resourceFilePath/$testFileStr is found: ${expectedPath != null}")
        }

        return compareFilesFromFileSystem(
            Paths.get(expectedPath.toURI()),
            Paths.get(testPath.toURI()),
            trimLastEmptyLine,
            replacements,
        )
    }

    /**
     * @param expectedFile the file which has the expected content. The trailing
     *   newline, if any, **won't be read** as a separate empty string. So, if
     *   the content to be read from this file is expected to be terminated with
     *   an empty string (which is the case if `newlineAtEnd` is `true`), then
     *   the file should end with **two** consecutive linebreaks.
     * @param testFile the file which has the original content.
     * @param trimLastEmptyLine whether the last (empty) line should be
     *   discarded when reading the content of [testFile].
     * @param replacements a map of replacements which will be applied to [expectedFile] and [testFile] before comparing.
     * @return the result of file comparison by their content.
     * @see compareFilesFromResources
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromFileSystem(
        expectedFile: Path,
        testFile: Path,
        trimLastEmptyLine: Boolean = false,
        replacements: Map<String, String> = emptyMap(),
    ): FileComparisonResult {
        if (!testFile.isRegularFile() || !expectedFile.isRegularFile()) {
            log.error("Not able to find files for running test: $expectedFile and $testFile")
            return FileComparisonResult(
                isSuccessful = false,
                delta = null,
                actualContent = "// $testFile is a regular file: ${testFile.isRegularFile()}",
                expectedContent = "// $expectedFile is a regular file: ${expectedFile.isRegularFile()}")
        }

        val copyTestFile = Path("${testFile.absolutePathString()}_copy")
        testFile.copyTo(copyTestFile, overwrite = true)

        val actualResult = function(
            readFile(copyTestFile).replaceAll(replacements).joinToString("\n"),
            copyTestFile.absolutePathString()
        )

        val actualFileContent = if (trimLastEmptyLine) {
            actualResult.split("\n").dropLast(1)
        } else {
            // fixme: actualResult is separated by KtLint#determineLneSeparator, should be split by it here too
            actualResult.split("\n")
        }

        val expectedFileContent = readFile(expectedFile).replaceAll(replacements)

        val comparator = FileComparator(
            expectedFile.name,
            expectedFileContent,
            testFile.name,
            actualFileContent,
        )

        return FileComparisonResult(
            comparator.compareFilesEqual(),
            comparator.delta,
            actualFileContent.joinToString("\n"),
            expectedFileContent.joinToString("\n"))
    }

    private companion object {
        private val log = KotlinLogging.logger {}

        /**
         * @param file the file whose content is to be read.
         * @return file content as a list of lines, or an empty list if an I/O error
         *   has occurred.
         */
        private fun readFile(file: Path): List<String> =
            try {
                file.readLines()
            } catch (e: IOException) {
                log.error("Not able to read file: $file")
                emptyList()
            }

        private fun List<String>.replaceAll(replacements: Map<String, String>): List<String> = map { line ->
            replacements.entries
                .fold(line) { result, replacement ->
                    result.replace(replacement.key, replacement.value)
                }
        }
    }
}
