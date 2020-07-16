package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.common.config.rules.RuleConfiguration

internal class IndentationConfig(config: Map<String, String>) : RuleConfiguration(config) {
    val newlineAtEnd: Boolean
        get() = config["newlineAtEnd"]?.toBoolean() ?: true

    /**
     * If true, in parameter list when parameters are split by newline they are indented with two indentations instead of one
     */
    val extendedIndentOfParameters: Boolean
        get() = config["extendedIndentOfParameters"]?.toBoolean() ?: true

    /**
     * If true, if first parameter in parameter list is on the same line as opening parenthesis, then other parameters
     * can be aligned with it
     */
    val alignedParameters: Boolean
        get() = config["alignedParameters"]?.toBoolean() ?: true

    /**
     * If true, if expression is split by newline after operator like +/-/`*`, then the next line is indented with two indentations instead of one
     */
    val extendedIndentAfterOperators: Boolean
        get() = config["extendedIndentAfterOperators"]?.toBoolean() ?: true
}
