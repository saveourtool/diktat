package org.cqfn.diktat.ruleset.rules.chapter3.files

import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig

/**
 * Higher-level abstractions on top of the [indentation size][IndentationConfig.indentationSize].
 */
internal interface IndentationConfigAware {
    /**
     * The configuration this instance encapsulates.
     */
    val configuration: IndentationConfig

    /**
     * Increases the indentation level by [level] * [IndentationConfig.indentationSize].
     *
     * This extension doesn't modify the receiver.
     *
     * @receiver the previous indentation level (in space characters), not
     *   modified by the function call.
     * @param level the indentation level, 1 by default.
     * @return the new indentation level.
     * @see unindent
     * @see IndentationConfig.indentationSize
     */
    fun Int.indent(level: Int = 1): Int =
        this + level * configuration.indentationSize

    /**
     * Decreases the indentation level by [level] * [IndentationConfig.indentationSize].
     *
     * This extension doesn't modify the receiver.
     *
     * @receiver the previous indentation level (in space characters), not
     *   modified by the function call.
     * @param level the indentation level, 1 by default.
     * @return the new indentation level.
     * @see indent
     * @see IndentationConfig.indentationSize
     */
    fun Int.unindent(level: Int = 1): Int =
        indent(-level)

    /**
     * @receiver the previous indentation level (in space characters), not
     *   modified by the function call.
     * @param amount the indentation amount.
     * @return the new (increased) indentation level.
     * @see minus
     */
    operator fun Int.plus(amount: IndentationAmount): Int =
        indent(level = amount.level())

    /**
     * @receiver the previous indentation level (in space characters), not
     *   modified by the function call.
     * @param amount the indentation amount.
     * @return the new (decreased) indentation level.
     * @see plus
     */
    operator fun Int.minus(amount: IndentationAmount): Int =
        unindent(level = amount.level())

    companion object Factory {
        /**
         * Creates a new instance.
         *
         * @param configuration the configuration this instance will wrap.
         * @return the newly created instance.
         */
        operator fun invoke(configuration: IndentationConfig): IndentationConfigAware =
            object : IndentationConfigAware {
                override val configuration = configuration
            }
    }
}
