package org.cqfn.diktat.test.framework.processing

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines

/**
 * Class that can apply transformation to an input file and then compare with expected result and output difference.
 *
 * @property resourceFilePath only used when the files are loaded as resources,
 *   via [compareFilesFromResources].
 * @property function a transformation that will be applied to the file
 */
@Suppress("ForbiddenComment", "TYPE_ALIAS")
class TestComparatorUnit(private val resourceFilePath: String,
                         private val function: (expectedText: String, testFilePath: String) -> String) {
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
     * @return the result of file comparison by their content.
     * @see compareFilesFromFileSystem
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromResources(
        expectedResult: String,
        testFileStr: String,
        trimLastEmptyLine: Boolean = false
    ): FileComparisonResult {
        val expectedPath = javaClass.classLoader.getResource("$resourceFilePath/$expectedResult")
        val testPath = javaClass.classLoader.getResource("$resourceFilePath/$testFileStr")
        if (testPath == null || expectedPath == null) {
            log.error("Not able to find files for running test: $expectedResult and $testFileStr")
            return FileComparisonResult(
                isSuccessful = false,
                actualContent = "// $resourceFilePath/$expectedResult is found: ${testPath != null}",
                expectedContent = "// $resourceFilePath/$testFileStr is found: ${expectedPath != null}")
        }

        return compareFilesFromFileSystem(
            Paths.get(expectedPath.toURI()),
            Paths.get(testPath.toURI()),
            trimLastEmptyLine)
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
     * @return the result of file comparison by their content.
     * @see compareFilesFromResources
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromFileSystem(
        expectedFile: Path,
        testFile: Path,
        trimLastEmptyLine: Boolean = false
    ): FileComparisonResult {
        if (!testFile.isRegularFile() || !expectedFile.isRegularFile()) {
            log.error("Not able to find files for running test: $expectedFile and $testFile")
            return FileComparisonResult(
                isSuccessful = false,
                actualContent = "// $testFile is a regular file: ${testFile.isRegularFile()}",
                expectedContent = "// $expectedFile is a regular file: ${expectedFile.isRegularFile()}")
        }

        val copyTestFile = Path("${testFile.absolutePathString()}_copy")
        testFile.copyTo(copyTestFile, overwrite = true)

        val actualResult = function(
            readFile(copyTestFile).joinToString("\n"),
            copyTestFile.absolutePathString()
        )

        val actualFileContent = if (trimLastEmptyLine) {
            actualResult.split("\n").dropLast(1)
        } else {
            // fixme: actualResult is separated by KtLint#determineLneSeparator, should be split by it here too
            actualResult.split("\n")
        }

        val expectedFileContent = readFile(expectedFile)

        val isSuccessful = FileComparator(
            expectedFile,
            expectedFileContent,
            testFile,
            actualFileContent).compareFilesEqual()

        return FileComparisonResult(
            isSuccessful,
            actualFileContent.joinToString("\n"),
            expectedFileContent.joinToString("\n"))
    }

    private companion object {
        private val log: Logger = LoggerFactory.getLogger(TestComparatorUnit::class.java)

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
    }
}
