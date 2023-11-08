/**
 * It's a class container for test file content.
 * Plus exception cases when resource or file is not found
 */

package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.util.describe

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language

import java.nio.file.Path

import kotlin.io.path.absolutePathString

/**
 * A base interface for content of test file
 */
sealed interface TestFileContent {
    /**
     * Asserts [TestFileContent] that content are equal
     */
    fun assertSuccessful()
}

/**
 * Implementation of [TestFileContent] when resources are not found
 *
 * @param expectedResource
 * @param expectedPath
 * @param actualResource
 * @param actualPath
 */
data class NotFoundResourcesTestFileContent(
    private val expectedResource: String,
    private val expectedPath: Path?,
    private val actualResource: String,
    private val actualPath: Path?,
) : TestFileContent {
    override fun assertSuccessful() {
        assertSoftly { softly ->
            softly.assertThat(expectedPath)
                .describedAs("Expected resource <%s>", expectedResource)
                .isNotNull
            softly.assertThat(actualPath)
                .describedAs("Actual resource <%s>", actualResource)
                .isNotNull
        }
    }
}

/**
 * Implementation of [TestFileContent] when files are not found
 *
 * @param expectedPath
 * @param actualPath
 */
data class NotFoundFilesTestFileContent(
    private val expectedPath: Path,
    private val actualPath: Path,
) : TestFileContent {
    override fun assertSuccessful() {
        assertSoftly { softly ->
            softly.assertThat(expectedPath)
                .describedAs("Expected file <%s>", expectedPath.absolutePathString())
                .isRegularFile
            softly.assertThat(actualPath)
                .describedAs("Actual resource <%s>", actualPath.absolutePathString())
                .isRegularFile
        }
    }
}

/**
 * The result of files being compared by their content.
 *
 * @param actualContent the actual file content (possibly slightly different
 *   from the original after `diktat:check` is run).
 * @param expectedContent the expected file content without warns.
 */
data class DefaultTestFileContent(
    @Language("kotlin") private val actualContent: String,
    @Language("kotlin") private val expectedContent: String,
) : TestFileContent {
    override fun assertSuccessful() {
        assertThat(actualContent)
            .describedAs("lint result for ", actualContent.describe())
            .isEqualTo(expectedContent)
    }
}
