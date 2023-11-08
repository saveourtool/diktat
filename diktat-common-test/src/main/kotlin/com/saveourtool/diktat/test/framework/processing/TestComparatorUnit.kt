package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.processing.ResourceReader.Companion.withPrefix
import com.saveourtool.diktat.test.framework.util.NEWLINE
import com.saveourtool.diktat.test.framework.util.readTextOrNull
import com.saveourtool.diktat.test.framework.util.toUnixEndLines
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * Class that can apply transformation to an input file and then compare with expected result and output difference.
 *
 * @param resourceReader only used when the files are loaded as resources,
 *   via [compareFilesFromResources].
 * @param function a transformation that will be applied to the file
 */
@Suppress("ForbiddenComment", "TYPE_ALIAS")
class TestComparatorUnit(
    private val resourceReader: ResourceReader = ResourceReader.default,
    private val function: (testFile: Path) -> String,
) {
    constructor(
        resourceFilePath: String,
        function: (testFile: Path) -> String,
    ) : this(
        resourceReader = ResourceReader.default.withPrefix(resourceFilePath),
        function = function,
    )

    /**
     * @param expectedResult the name of the resource which has the expected
     *   content. The trailing newline, if any, **won't be read** as a separate
     *   empty string. So, if the content to be read from this file is expected
     *   to be terminated with an empty string (which is the case if
     *   `newlineAtEnd` is `true`), then the file should end with **two**
     *   consecutive linebreaks.
     * @param testFileStr the name of the resource which has the original content.
     * @param overrideResourceReader function to override [ResourceReader] to read resource content
     * @return the result of file comparison by their content.
     * @see compareFilesFromFileSystem
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesFromResources(
        expectedResult: String,
        testFileStr: String,
        overrideResourceReader: (ResourceReader) -> ResourceReader = { it },
    ): TestFileContent {
        val overriddenResourceReader = overrideResourceReader(resourceReader)
        val expectedPath = overriddenResourceReader(expectedResult)
        val testPath = overriddenResourceReader(testFileStr)
        if (testPath == null || expectedPath == null) {
            log.error { "Not able to find files for running test: $expectedResult and $testFileStr" }
            return NotFoundResourcesTestFileContent(
                expectedResource = expectedResult,
                expectedPath = expectedPath,
                actualResource = testFileStr,
                actualPath = testPath,
            )
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
    ): TestFileContent {
        if (!testFile.isRegularFile() || !expectedFile.isRegularFile()) {
            log.error { "Not able to find files for running test: $expectedFile and $testFile" }
            return NotFoundFilesTestFileContent(
                expectedPath = expectedFile,
                actualPath = testFile,
            )
        }

        val actualFileContent = function(testFile).toUnixEndLines()
        val expectedFileContent = expectedFile.readTextOrNull().orEmpty()

        return DefaultTestFileContent(
            actualContent = actualFileContent,
            expectedContent = expectedFileContent.withoutWarns(),
        )
    }

    private companion object {
        private val log = KotlinLogging.logger {}
        private val warnRegex = (".*// ;warn:?(.*):(\\d*): (.+)").toRegex()

        /**
         * @return Expected result without lines with warns
         */
        private fun String.withoutWarns(): String = split(NEWLINE)
            .filterNot { line ->
                line.contains(warnRegex)
            }
            .joinToString(NEWLINE.toString())
    }
}
