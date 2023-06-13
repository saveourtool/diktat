package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.Extension
import java.util.SortedMap

/**
 * @property customConfig non-default configuration for the indentation rule.
 * @property actualCode the original file content (may well get modified as
 *   fixes are applied).
 * @property expectedCode the content the file is expected to have after the
 *   fixes are applied.
 */
@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
class IndentationTestFixInvocationContext(
    override val customConfig: SortedMap<String, out Any>,
    @Language("kotlin") override val actualCode: String,
    @Language("kotlin") private val expectedCode: String = actualCode
) : IndentationTestInvocationContext {
    override val mode: String = "fix"

    override val correctlyIndented: Boolean = actualCode == expectedCode

    override val displayName: String = when {
        correctlyIndented -> "should remain unchanged if properly indented"
        else -> "should be reformatted if mis-indented"
    }

    override fun getAdditionalExtensions(): List<Extension> =
        listOf(IndentationTestFixExtension(customConfig, actualCode, expectedCode))
}
