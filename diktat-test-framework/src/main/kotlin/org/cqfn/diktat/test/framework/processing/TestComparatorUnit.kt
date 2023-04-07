package org.cqfn.diktat.test.framework.processing

import org.cqfn.diktat.test.framework.util.readTextOrNull
import org.cqfn.diktat.test.framework.util.toUnixEndLines
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

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
    private val function: (testFile: Path) -> String,
) {
    /**
     * @param expectedResult the name of the resource which has the expected
     *   content. The trailing newline, if any, **won't be read** as a separate
     *   empty string. So, if the content to be read from this file is expected
     *   to be terminated with an empty string (which is the case if
     *   `newlineAtEnd` is `true`), then the file should end with **two**
     *   consecutive linebreaks.
     * @param testFileStr the name of the resource which has the original content.
     * @param resourceReader [ResourceReader] to read resource content
     * @return the result of file comparison by their content.
     * @see compareFilesFromFileSystem
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromResources(
        expectedResult: String,
        testFileStr: String,
        resourceReader: ResourceReader = ResourceReader.default,
    ): FileComparisonResult {
        val expectedPath = resourceReader("$resourceFilePath/$expectedResult")
        val testPath = resourceReader("$resourceFilePath/$testFileStr")
        if (testPath == null || expectedPath == null) {
            log.error("Not able to find files for running test: $expectedResult and $testFileStr")
            return FileComparisonResult(
                isSuccessful = false,
                delta = null,
                actualContent = "// $resourceFilePath/$expectedResult is found: ${testPath != null}",
                expectedContent = "// $resourceFilePath/$testFileStr is found: ${expectedPath != null}")
        }

        return compareFilesFromFileSystem(
            expectedPath,
            testPath,
        )
    }

    /**
     * @param expectedFile the file which has the expected content. The trailing
     *   newline, if any, **won't be read** as a separate empty string. So, if
     *   the content to be read from this file is expected to be terminated with
     *   an empty string (which is the case if `newlineAtEnd` is `true`), then
     *   the file should end with **two** consecutive linebreaks.
     * @param testFile the file which has the original content.
     * @return the result of file comparison by their content.
     * @see compareFilesFromResources
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromFileSystem(
        expectedFile: Path,
        testFile: Path,
    ): FileComparisonResult {
        if (!testFile.isRegularFile() || !expectedFile.isRegularFile()) {
            log.error("Not able to find files for running test: $expectedFile and $testFile")
            return FileComparisonResult(
                isSuccessful = false,
                delta = null,
                actualContent = "// $testFile is a regular file: ${testFile.isRegularFile()}",
                expectedContent = "// $expectedFile is a regular file: ${expectedFile.isRegularFile()}")
        }

        val actualFileContent = function(testFile).toUnixEndLines()
        val expectedFileContent = expectedFile.readTextOrNull().orEmpty()

        val comparator = FileComparator(
            expectedFile.name,
            expectedFileContent,
            actualFileContent,
        )

        return FileComparisonResult(
            comparator.compareFilesEqual(),
            comparator.delta,
            actualFileContent,
            expectedFileContent)
    }

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}
