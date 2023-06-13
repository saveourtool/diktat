package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.junit.BooleanOrDefault
import com.saveourtool.diktat.ruleset.junit.BooleanOrDefault.DEFAULT
import org.intellij.lang.annotations.Language
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Requirements for [code]:
 *
 * - the code should contain at least one expected-error annotation in the
 *   format `diktat:<CHECK_NAME>` or `diktat:<CHECK_NAME>[name1 = value1, name2 = value2]`;
 * - the exact format is `diktat:WRONG_INDENTATION[expectedIndent = <expected indent>]`;
 * - the `expectedIndent` property should be present and its value should be a
 *   number;
 * - the values of expected and actual indent (inferred from the code) should be
 *   different;
 * - the code should be non-blank.
 *
 * @property code the source code to test. Common indentation will be trimmed using
 *   [String.trimIndent].
 * @property extendedIndentOfParameters describes the effective formatting of [code].
 * @property extendedIndentForExpressionBodies describes the effective formatting of [code].
 * @property extendedIndentAfterOperators describes the effective formatting of [code].
 * @property extendedIndentBeforeDot describes the effective formatting of [code].
 */
@Target
@Retention(RUNTIME)
@MustBeDocumented
annotation class IndentedSourceCode(
    @Language("kotlin") val code: String,
    val extendedIndentOfParameters: BooleanOrDefault = DEFAULT,
    val extendedIndentForExpressionBodies: BooleanOrDefault = DEFAULT,
    val extendedIndentAfterOperators: BooleanOrDefault = DEFAULT,
    val extendedIndentBeforeDot: BooleanOrDefault = DEFAULT,
)
