package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.chapter3.spaces.ExpectedIndentationError
import com.saveourtool.diktat.ruleset.junit.RuleInvocationContextProvider
import com.saveourtool.diktat.ruleset.utils.NEWLINE
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.leadingSpaceCount
import com.saveourtool.diktat.util.assertNotNull
import generated.WarningNames.WRONG_INDENTATION
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.platform.commons.util.AnnotationUtils.findAnnotation
import java.util.SortedMap
import java.util.stream.Stream
import java.util.stream.Stream.concat
import kotlin.reflect.KClass

/**
 * The `TestTemplateInvocationContextProvider` implementation for indentation
 * tests.
 */
class IndentationTestInvocationContextProvider : RuleInvocationContextProvider<IndentationTest, ExpectedIndentationError> {
    override fun annotationType(): KClass<IndentationTest> =
        IndentationTest::class

    override fun expectedLintErrorFrom(
        @Language("kotlin") line: String,
        lineNumber: Int,
        tag: String,
        properties: Map<String, String?>
    ): ExpectedIndentationError {
        val message = properties[MESSAGE]
        @Suppress("AVOID_NULL_CHECKS")
        if (message != null) {
            return ExpectedIndentationError(
                line = lineNumber,
                message = "[$WRONG_INDENTATION] $message")
        }

        val expectedIndent = properties.expectedIndent()
        val actualIndent = line.leadingSpaceCount()

        assertThat(actualIndent)
            .describedAs("Expected and actual indent values are the same: $expectedIndent, line $lineNumber (\"$line\")")
            .isNotEqualTo(expectedIndent)

        assertThat(tag)
            .describedAs("Unexpected tag: $tag")
            .isEqualTo(WRONG_INDENTATION)

        return ExpectedIndentationError(
            line = lineNumber,
            expectedIndent = expectedIndent,
            actualIndent = actualIndent)
    }

    @Suppress("TOO_LONG_FUNCTION")
    override fun provideTestTemplateInvocationContexts(context: ExtensionContext, supportedTags: List<String>): Stream<TestTemplateInvocationContext> {
        val testMethod = context.requiredTestMethod

        val indentationTest = findAnnotation(testMethod, annotationType().java).get()

        val includeWarnTests = indentationTest.includeWarnTests
        val singleConfiguration = indentationTest.singleConfiguration

        val testInput0 = indentationTest.first.extractTestInput(
            supportedTags,
            allowEmptyErrors = !includeWarnTests || singleConfiguration)
        val (code0, expectedErrors0, customConfig0) = testInput0

        var contexts: Stream<TestTemplateInvocationContext> = Stream.of(
            IndentationTestFixInvocationContext(customConfig0, actualCode = code0)
        )

        if (includeWarnTests) {
            /*-
             * In a double-configuration mode (the default), when the code is
             * checked against its own configuration, the actual list of errors
             * is expected to be empty (it's only used when the code is checked
             * against the opposite configuration.
             *
             * In a single-configuration mode, the opposite configuration is
             * empty, so let's allow a non-empty list of expected errors when
             * the code is checked against its own configuration.
             */
            val expectedErrors = when {
                singleConfiguration -> expectedErrors0
                else -> emptyList()
            }
            contexts += IndentationTestWarnInvocationContext(customConfig0, actualCode = code0, expectedErrors)
        }

        when {
            singleConfiguration -> {
                val code1 = indentationTest.second.code
                assertThat(code1)
                    .describedAs("The 2nd code fragment should be empty if `singleConfiguration` is `true`: $NEWLINE$code1")
                    .isEmpty()
            }

            else -> {
                val testInput1 = indentationTest.second.extractTestInput(
                    supportedTags,
                    allowEmptyErrors = !includeWarnTests)
                val (code1, expectedErrors1, customConfig1) = testInput1

                assertThat(code0)
                    .describedAs("Both code fragments are the same")
                    .isNotEqualTo(code1)
                assertThat(customConfig0)
                    .describedAs("Both custom configs are the same")
                    .isNotEqualTo(customConfig1)
                assertThat(testInput0.effectiveConfig)
                    .describedAs("Both effective configs are the same")
                    .isNotEqualTo(testInput1.effectiveConfig)

                contexts += IndentationTestFixInvocationContext(customConfig1, actualCode = code1)
                contexts += IndentationTestFixInvocationContext(customConfig1, actualCode = code0, expectedCode = code1)
                contexts += IndentationTestFixInvocationContext(customConfig0, actualCode = code1, expectedCode = code0)

                if (includeWarnTests) {
                    contexts += IndentationTestWarnInvocationContext(customConfig1, actualCode = code1)
                    contexts += IndentationTestWarnInvocationContext(customConfig1, actualCode = code0, expectedErrors0)
                    contexts += IndentationTestWarnInvocationContext(customConfig0, actualCode = code1, expectedErrors1)
                }
            }
        }

        return contexts.sorted { left, right ->
            left.getDisplayName(0).compareTo(right.getDisplayName(0))
        }
    }

    /**
     * @param allowEmptyErrors whether the list of expected errors is allowed to
     *   be empty (i.e. the code may contain no known annotations).
     */
    private fun IndentedSourceCode.extractTestInput(supportedTags: List<String>,
                                                    allowEmptyErrors: Boolean): IndentationTestInput {
        val (code, expectedErrors) = extractExpectedErrors(code, supportedTags, allowEmptyErrors)

        return IndentationTestInput(code, expectedErrors, customConfig())
    }

    private companion object {
        private const val EXPECTED_INDENT = "expectedIndent"
        private const val MESSAGE = "message"

        @Suppress("WRONG_NEWLINES")  // False positives, see #1495.
        private fun IndentedSourceCode.customConfig(): SortedMap<String, out Boolean> =
            mapOf(
                EXTENDED_INDENT_AFTER_OPERATORS to extendedIndentAfterOperators,
                EXTENDED_INDENT_BEFORE_DOT to extendedIndentBeforeDot,
                EXTENDED_INDENT_FOR_EXPRESSION_BODIES to extendedIndentForExpressionBodies,
                EXTENDED_INDENT_OF_PARAMETERS to extendedIndentOfParameters,
            ).mapValues { (_, value) ->
                value.valueOrNull
            }.filterValues { value ->
                value != null
            }.mapValues { (_, value) ->
                value!!
            }.toSortedMap()

        private fun Map<String, String?>.expectedIndent(): Int {
            val expectedIndentRaw = this[EXPECTED_INDENT].assertNotNull {
                "There's no `$EXPECTED_INDENT` key in $this"
            }

            assertThat(expectedIndentRaw)
                .describedAs("`$EXPECTED_INDENT` is empty")
                .isNotEmpty

            return try {
                expectedIndentRaw.toInt()
            } catch (_: NumberFormatException) {
                fail("Unparseable `$EXPECTED_INDENT`: $expectedIndentRaw")
            }
        }

        private operator fun <T> Stream<T>.plus(value: T): Stream<T> =
            concat(this, Stream.of(value))
    }
}
