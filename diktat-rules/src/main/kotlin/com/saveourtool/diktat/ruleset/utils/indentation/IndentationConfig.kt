package com.saveourtool.diktat.ruleset.utils.indentation

import com.saveourtool.diktat.api.DiktatErrorEmitter
import com.saveourtool.diktat.common.config.rules.RuleConfiguration

/**
 * [RuleConfiguration] for indentation logic
 */
class IndentationConfig(config: Map<String, String>) : RuleConfiguration(config) {
    /**
     * Is newline at the end of a file needed
     */
    val newlineAtEnd = config[NEWLINE_AT_END]?.toBoolean() ?: true

    /**
     * If true, in parameter list when parameters are split by newline they are indented with two indentations instead of one
     */
    val extendedIndentOfParameters = config[EXTENDED_INDENT_OF_PARAMETERS]?.toBoolean() ?: false

    /**
     * If true, if first parameter in parameter list is on the same line as opening parenthesis, then other parameters
     * can be aligned with it
     */
    val alignedParameters = config[ALIGNED_PARAMETERS]?.toBoolean() ?: true

    /**
     * If `true`, expression bodies which begin on a separate line are indented
     * using a _continuation indent_. The flag is **off**:
     *
     * ```kotlin
     * val a: Boolean =
     *     false
     *
     * val b: Boolean
     *     get() =
     *         false
     *
     * fun f(): Boolean =
     *     false
     * ```
     *
     *  The flag is **on**:
     *
     * ```kotlin
     * val a: Boolean =
     *         false
     *
     * val b: Boolean
     *     get() =
     *             false
     *
     * fun f(): Boolean =
     *         false
     * ```
     *
     * The default is `false`.
     *
     * This flag is called `CONTINUATION_INDENT_FOR_EXPRESSION_BODIES` in _IDEA_
     * and `ij_kotlin_continuation_indent_for_expression_bodies` in
     * `.editorconfig`.
     *
     * @since 1.2.2
     */
    val extendedIndentForExpressionBodies = config[EXTENDED_INDENT_FOR_EXPRESSION_BODIES]?.toBoolean() ?: false

    /**
     * If true, if expression is split by newline after operator like +/-/`*`, then the next line is indented with two indentations instead of one
     */
    val extendedIndentAfterOperators = config[EXTENDED_INDENT_AFTER_OPERATORS]?.toBoolean() ?: true

    /**
     * If true, when dot qualified expression starts on a new line, this line will be indented with
     * two indentations instead of one
     */
    val extendedIndentBeforeDot = config[EXTENDED_INDENT_BEFORE_DOT]?.toBoolean() ?: false

    /**
     * The indentation size for each file
     */
    val indentationSize = config[INDENTATION_SIZE]?.toInt() ?: DEFAULT_INDENTATION_SIZE

    override fun equals(other: Any?): Boolean =
        other is IndentationConfig && configWithExplicitDefaults == other.configWithExplicitDefaults

    override fun hashCode(): Int =
        configWithExplicitDefaults.hashCode()

    override fun toString(): String =
        "${javaClass.simpleName}$configWithExplicitDefaults"

    /**
     * @param errorEmitter
     * @return overridden [errorEmitter] which warns message with configured [indentationSize]
     */
    fun overrideIfRequiredWarnMessage(errorEmitter: DiktatErrorEmitter): DiktatErrorEmitter = if (indentationSize == DEFAULT_INDENTATION_SIZE) {
        errorEmitter
    } else {
        DiktatErrorEmitter { offset, errorMessage, canBeAutoCorrected ->
            errorEmitter(offset, errorMessage.replace("should equal to 4 spaces (tabs are not allowed)", "should equal to $indentationSize spaces (tabs are not allowed)"),
                canBeAutoCorrected)
        }
    }

    companion object {
        internal const val ALIGNED_PARAMETERS = "alignedParameters"

        /**
         * The default indent size (space characters), configurable via
         * `indentationSize`.
         */
        private const val DEFAULT_INDENTATION_SIZE = 4
        const val EXTENDED_INDENT_AFTER_OPERATORS = "extendedIndentAfterOperators"
        const val EXTENDED_INDENT_BEFORE_DOT = "extendedIndentBeforeDot"
        const val EXTENDED_INDENT_FOR_EXPRESSION_BODIES = "extendedIndentForExpressionBodies"
        const val EXTENDED_INDENT_OF_PARAMETERS = "extendedIndentOfParameters"
        const val INDENTATION_SIZE = "indentationSize"
        const val NEWLINE_AT_END = "newlineAtEnd"

        @Suppress("CUSTOM_GETTERS_SETTERS")
        private val IndentationConfig.configWithExplicitDefaults: Map<String, String>
            get() =
                mutableMapOf<String, Any>().apply {
                    putAll(config)
                    putIfAbsent(ALIGNED_PARAMETERS, alignedParameters)
                    putIfAbsent(EXTENDED_INDENT_AFTER_OPERATORS, extendedIndentAfterOperators)
                    putIfAbsent(EXTENDED_INDENT_BEFORE_DOT, extendedIndentBeforeDot)
                    putIfAbsent(EXTENDED_INDENT_FOR_EXPRESSION_BODIES, extendedIndentForExpressionBodies)
                    putIfAbsent(EXTENDED_INDENT_OF_PARAMETERS, extendedIndentOfParameters)
                    putIfAbsent(INDENTATION_SIZE, indentationSize)
                    putIfAbsent(NEWLINE_AT_END, newlineAtEnd)
                }.mapValues { (_, value) ->
                    value.toString()
                }
    }
}
