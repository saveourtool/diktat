package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.common.config.rules.RuleConfiguration

/**
 * [RuleConfiguration] for indentation logic
 */
internal class IndentationConfig(config: Map<String, String>) : RuleConfiguration(config) {
    /**
     * Is newline at the end of a file needed
     */
    val newlineAtEnd = config["newlineAtEnd"]?.toBoolean() ?: true

    /**
     * If true, in parameter list when parameters are split by newline they are indented with two indentations instead of one
     */
    val extendedIndentOfParameters = config["extendedIndentOfParameters"]?.toBoolean() ?: false

    /**
     * If true, if first parameter in parameter list is on the same line as opening parenthesis, then other parameters
     * can be aligned with it
     */
    val alignedParameters = config["alignedParameters"]?.toBoolean() ?: true

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
    val extendedIndentForExpressionBodies = config["extendedIndentForExpressionBodies"]?.toBoolean() ?: false

    /**
     * If true, if expression is split by newline after operator like +/-/`*`, then the next line is indented with two indentations instead of one
     */
    val extendedIndentAfterOperators = config["extendedIndentAfterOperators"]?.toBoolean() ?: false

    /**
     * If true, when dot qualified expression starts on a new line, this line will be indented with
     * two indentations instead of one
     */
    val extendedIndentBeforeDot = config["extendedIndentBeforeDot"]?.toBoolean() ?: false

    /**
     * The indentation size for each file
     */
    val indentationSize = config["indentationSize"]?.toInt() ?: DEFAULT_INDENT_SIZE

    private companion object {
        /**
         * The default indent size (space characters), configurable via
         * `indentationSize`.
         */
        private const val DEFAULT_INDENT_SIZE = 4
    }
}
