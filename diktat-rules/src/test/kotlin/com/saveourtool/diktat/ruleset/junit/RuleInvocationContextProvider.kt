package com.saveourtool.diktat.ruleset.junit

import com.saveourtool.diktat.ruleset.utils.NEWLINE
import generated.WarningNames
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.platform.commons.util.AnnotationUtils.isAnnotated
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

/**
 * A common super-interface for rule-specific
 * [TestTemplateInvocationContextProvider] implementations.
 */
interface RuleInvocationContextProvider<A : Annotation, out E : ExpectedLintError> : TestTemplateInvocationContextProvider {
    /**
     * @return the [TestTemplate] annotation supported by this
     *   [TestTemplateInvocationContextProvider] implementation.
     */
    fun annotationType(): KClass<A>

    override fun supportsTestTemplate(context: ExtensionContext): Boolean =
        isAnnotated(context.testMethod, annotationType().java)

    /**
     * @param context the extension context for the test template method about
     *   to be invoked.
     * @param supportedTags the list of check names that should be recognized
     *   (implementation-dependent).
     * @return a `Stream` of `TestTemplateInvocationContext` instances for the
     *   invocation of the test template method.
     */
    fun provideTestTemplateInvocationContexts(context: ExtensionContext, supportedTags: List<String>): Stream<TestTemplateInvocationContext>

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> {
        @Suppress("WRONG_NEWLINES")  // False positives, see #1495.
        val supportedTags = context
            .tags
            .asSequence()
            .filter { tag ->
                tag in warningNames
            }.toList()

        assertThat(supportedTags).describedAs("Please annotate `${annotationType().simpleName}` with `@Tag`").isNotEmpty

        return provideTestTemplateInvocationContexts(context, supportedTags)
    }

    /**
     * Creates an (expected) lint error from the parsed annotation data.
     *
     * @param line the line of code which had the annotation (with the
     *   annotation stripped).
     * @param lineNumber the 1-based line number.
     * @param tag the name of the check, one of [WarningNames].
     * @param properties the properties of the lint error, if any (the map may
     *   be empty).
     * @return the lint error created.
     */
    fun expectedLintErrorFrom(
        @Language("kotlin") line: String,
        lineNumber: Int,
        tag: String,
        properties: Map<String, String?>
    ): E

    /**
     * Extracts list errors from the annotated code, using
     * [expectedLintErrorFrom] as the factory method.
     *
     * @param code the annotated code.
     * @param supportedTags the list of check names that should be recognized
     *   (implementation-dependent).
     * @param allowEmptyErrors whether the list of expected errors is allowed to
     *   be empty (i.e. the code may contain no known annotations).
     * @return the list of expected errors as well as the filtered code.
     * @see expectedLintErrorFrom
     */
    @Suppress("TOO_LONG_FUNCTION")
    fun extractExpectedErrors(@Language("kotlin") code: String,
                              supportedTags: List<String>,
                              allowEmptyErrors: Boolean
    ): ExpectedLintErrors<E> {
        require(supportedTags.isNotEmpty()) {
            "The list of supported tags is empty"
        }

        val codeAnnotationRegex = codeAnnotationRegex(supportedTags)

        val expectedErrors: MutableList<E> = mutableListOf()

        @Suppress(
            "AVOID_NULL_CHECKS",
            "WRONG_NEWLINES")  // False positives, see #1495.
        val filteredCode = code
            .trimIndent()
            .lineSequence()
            .mapIndexed { index, line ->
                extractExpectedError(index, line, codeAnnotationRegex)
            }.map { (line, expectedError) ->
                if (expectedError != null) {
                    expectedErrors += expectedError
                }

                line
            }.joinToString(separator = NEWLINE.toString())

        assertThat(filteredCode)
            .describedAs("The code is empty, please add some")
            .isNotEmpty
        assertThat(filteredCode)
            .describedAs("The code is blank, please add some non-whitespace")
            .isNotBlank
        val supportedTagsDescription = when (supportedTags.size) {
            1 -> supportedTags[0]
            else -> "any of $supportedTags"
        }
        if (!allowEmptyErrors) {
            assertThat(expectedErrors)
                .describedAs("The code contains no expected-error annotations or an unsupported tag is used (should be $supportedTagsDescription). " +
                        "Please annotate your code or set `includeWarnTests` to `false`:$NEWLINE$filteredCode")
                .isNotEmpty
        }

        return ExpectedLintErrors(filteredCode, expectedErrors)
    }

    @Suppress("NESTED_BLOCK")
    private fun extractExpectedError(
        index: Int,
        line: String,
        codeAnnotationRegex: Regex
    ): Pair<String, E?> =
        when (val result = codeAnnotationRegex.matchEntire(line)) {
            null -> line to null
            else -> {
                val groups = result.groups
                val filteredLine = groups[CODE]?.value ?: line

                val expectedError = when (val tag = groups[TAG]?.value) {
                    null -> null
                    else -> {
                        val properties = when (val rawProperties = groups[PROPERTIES]?.value) {
                            null -> emptyMap()
                            else -> parseProperties(rawProperties)
                        }

                        expectedLintErrorFrom(
                            line = filteredLine,
                            lineNumber = index + 1,
                            tag = tag,
                            properties = properties)
                    }
                }

                filteredLine to expectedError
            }
        }

    private companion object {
        private const val CODE = "code"

        /**
         * The common prefix code annotation comments will have.
         */
        private const val CODE_ANNOTATION_PREFIX = "diktat"

        @Language("RegExp")
        private const val KEY = """[^=,\h]+"""
        private const val KEY_GROUP = "key"
        private const val PROPERTIES = "properties"
        private const val TAG = "tag"

        @Language("RegExp")
        private const val VALUE = """[^,\]]*?"""
        private const val VALUE_GROUP = "value"
        private val entryRegex = Regex("""\h*(?<$KEY_GROUP>$KEY)\h*=\h*(?<$VALUE_GROUP>$VALUE)\h*""")

        @Suppress(
            "WRONG_NEWLINES",  // False positives, see #1495.
            "BLANK_LINE_BETWEEN_PROPERTIES")  // False positives, see #1496.
        private val warningNames = WarningNames::class
            .declaredMemberProperties
            .asSequence()
            .map { property ->
                property.name
            }.toSet()

        /**
         * Matches single-line (trailing) comments which contain strings like
         *
         * ```
         * diktat:CHECK_NAME
         * ```
         *
         * or
         *
         * ```
         * diktat:CHECK_NAME[name1 = value1, name2 = value2]
         * ```
         * Example:
         *
         * ```
         * diktat:WRONG_INDENTATION[expectedIndent = 4]
         * ```
         */
        private fun codeAnnotationRegex(supportedTags: List<String>): Regex {
            require(supportedTags.isNotEmpty()) {
                "The list of supported tags is empty"
            }

            val tagRegex = supportedTags.asSequence().map { tag ->
                """\Q$tag\E"""
            }.joinToString(prefix = "(?<$TAG>", separator = "|", postfix = ")")

            return Regex("""^(?<$CODE>.*?)\h*//\h*\Q$CODE_ANNOTATION_PREFIX\E:$tagRegex(?:\[\h*(?<$PROPERTIES>$KEY\h*=\h*$VALUE\h*(?:,\h*$KEY\h*=\h*$VALUE\h*?)*)\h*,?\h*])?\h*$""")
        }

        private fun parseProperties(rawProperties: String): Map<String, String?> =
            rawProperties.splitToSequence(',').mapNotNull { entry ->
                entryRegex.matchEntire(entry)?.let { result ->
                    val groups = result.groups
                    val key = groups[KEY_GROUP]?.value
                    val value = groups[VALUE_GROUP]?.value

                    when (key) {
                        null -> null
                        else -> key to value
                    }
                }
            }.toMap()
    }
}
