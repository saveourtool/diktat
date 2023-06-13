package com.saveourtool.diktat.ruleset.junit

import com.saveourtool.diktat.ruleset.utils.leadingSpaceCount
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language

/**
 * @property code the filtered code w/o any annotation markers.
 * @property expectedErrors the list of expected lint errors.
 */
data class ExpectedLintErrors<out E : ExpectedLintError>(
    @Language("kotlin") val code: String,
    val expectedErrors: List<E>,
) {
    init {
        code.checkIndentTrimmed()
    }

    private companion object {
        private fun String.checkIndentTrimmed() {
            val commonIndent = lineSequence()
                .filter(String::isNotEmpty)
                .map(String::leadingSpaceCount)
                .minOrNull() ?: return

            assertThat(commonIndent)
                .describedAs("The whole code fragment is indented with $commonIndent space(s). Did you forget to call `trimIndent()`?")
                .isZero
        }
    }
}
