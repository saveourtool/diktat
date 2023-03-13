package org.cqfn.diktat.test.framework.processing

import org.intellij.lang.annotations.Language

/**
 * The result of files being compared by their content.
 *
 * @property isSuccessful `true` if file content match (the comparison is
 *   successful), `false` otherwise. Even if [isSuccessful] is `true`,
 *   [actualContent] and [expectedContent] are not necessarily the same
 *   (theoretically, they may differ by the amount of trailing newlines).
 *   Similarly, if [isSuccessful] is `false`, [actualContent] and
 *   [expectedContent] are not necessarily different (consider the case when
 *   both files are missing).
 * @property delta a delta between compared files.
 * @property actualContent the actual file content (possibly slightly different
 *   from the original after `diktat:check` is run).
 * @property expectedContent the expected file content.
 */
data class FileComparisonResult(
    val isSuccessful: Boolean,
    val delta: String?,
    @Language("kotlin") val actualContent: String,
    @Language("kotlin") val expectedContent: String
)
