package org.cqfn.diktat.ruleset.chapter3.spaces.junit

import org.cqfn.diktat.ruleset.chapter3.spaces.ExpectedIndentationError
import org.cqfn.diktat.ruleset.junit.RuleInvocationContextProvider
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import org.cqfn.diktat.ruleset.utils.leadingSpaceCount
import org.cqfn.diktat.util.assertNotNull
import generated.WarningNames.WRONG_INDENTATION
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.platform.commons.util.AnnotationUtils.findAnnotation
import java.util.SortedMap
import java.util.stream.Stream
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

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext, supportedTags: List<String>): Stream<TestTemplateInvocationContext> {
        val testMethod = context.requiredTestMethod

        val indentationTest = findAnnotation(testMethod, annotationType().java).get()

        val testInput0 = indentationTest.first.extractTestInput(supportedTags)
        val (code0, expectedErrors0, customConfig0) = testInput0

        val testInput1 = indentationTest.second.extractTestInput(supportedTags)
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

        return Stream.of<TestTemplateInvocationContext>(
            IndentationTestWarnInvocationContext(customConfig0, actualCode = code0),
            IndentationTestWarnInvocationContext(customConfig1, actualCode = code1),
            IndentationTestWarnInvocationContext(customConfig1, actualCode = code0, expectedErrors0),
            IndentationTestWarnInvocationContext(customConfig0, actualCode = code1, expectedErrors1),
            IndentationTestFixInvocationContext(customConfig0, actualCode = code0),
            IndentationTestFixInvocationContext(customConfig1, actualCode = code1),
            IndentationTestFixInvocationContext(customConfig1, actualCode = code0, expectedCode = code1),
            IndentationTestFixInvocationContext(customConfig0, actualCode = code1, expectedCode = code0),
        ).sorted { left, right ->
            left.getDisplayName(0).compareTo(right.getDisplayName(0))
        }
    }

    private fun IndentedSourceCode.extractTestInput(supportedTags: List<String>): IndentationTestInput {
        val (code, expectedErrors) = extractExpectedErrors(code, supportedTags)

        return IndentationTestInput(code, expectedErrors, customConfig())
    }

    private companion object {
        private const val EXPECTED_INDENT = "expectedIndent"

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
    }
}
