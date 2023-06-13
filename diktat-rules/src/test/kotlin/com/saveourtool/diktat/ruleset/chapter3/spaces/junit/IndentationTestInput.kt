@file:Suppress("FILE_UNORDERED_IMPORTS")// False positives, see #1494.

package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.chapter3.spaces.ExpectedIndentationError
import com.saveourtool.diktat.ruleset.chapter3.spaces.withCustomParameters
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.NEWLINE_AT_END
import org.intellij.lang.annotations.Language
import java.util.SortedMap
import com.saveourtool.diktat.ruleset.chapter3.spaces.IndentationConfigFactory as IndentationConfig

/**
 * The test data for indentation tests, extracted from annotations.
 *
 * @property code the code to check.
 * @property expectedErrors the expected lint errors (may be empty).
 * @property customConfig non-default configuration for the indentation rule.
 */
data class IndentationTestInput(
    @Language("kotlin") val code: String,
    val expectedErrors: List<ExpectedIndentationError>,
    val customConfig: SortedMap<String, out Any>,
) {
    /**
     * The effective configuration for the indentation rule (contains both
     * default and non-default entries).
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val effectiveConfig: Map<String, String>
        get() =
            IndentationConfig(NEWLINE_AT_END to false).withCustomParameters(customConfig)
}
