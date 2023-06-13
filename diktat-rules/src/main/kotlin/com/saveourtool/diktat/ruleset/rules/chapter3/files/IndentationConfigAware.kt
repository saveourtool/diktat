package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig

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

    /**
     * Allows the `+` operation between an Int and an IndentationAmount to be
     * commutative. Now, the following are equivalent:
     *
     * ```kotlin
     * val i = 42 + IndentationAmount.SINGLE
     * val j = IndentationAmount.SINGLE + 42
     * ```
     *
     * &mdash; as are these:
     *
     * ```kotlin
     * val i = 42 + IndentationAmount.SINGLE
     * val j = IndentationAmount.SINGLE + 42
     * ```
     *
     * @receiver the indentation amount.
     * @param indentationSpaces the indentation level (in space characters).
     * @return the new (increased) indentation level.
     * @see IndentationAmount.minus
     */
    operator fun IndentationAmount.plus(indentationSpaces: Int): Int =
        indentationSpaces + this

    /**
     * Allows expressions like this:
     *
     * ```kotlin
     * 42 - IndentationAmount.SINGLE + 4
     * ```
     *
     * to be rewritten this way:
     *
     * ```kotlin
     * 42 - (IndentationAmount.SINGLE - 4)
     * ```
     *
     * @receiver the indentation amount.
     * @param indentationSpaces the indentation level (in space characters).
     * @return the new (decreased) indentation level.
     * @see IndentationAmount.plus
     */
    operator fun IndentationAmount.minus(indentationSpaces: Int): Int =
        this + (-indentationSpaces)

    /**
     * @receiver the 1st term.
     * @param other the 2nd term.
     * @return the two indentation amounts combined, as the indentation level
     *   (in space characters).
     * @see IndentationAmount.minus
     */
    operator fun IndentationAmount.plus(other: IndentationAmount): Int =
        this + (+other)

    /**
     * @receiver the minuend.
     * @param other the subtrahend.
     * @return one amount subtracted from the other, as the indentation level
     *   (in space characters).
     * @see IndentationAmount.plus
     */
    operator fun IndentationAmount.minus(other: IndentationAmount): Int =
        this + (-other)

    /**
     * @receiver the indentation amount.
     * @return the indentation level (in space characters).
     * @see IndentationAmount.unaryMinus
     */
    operator fun IndentationAmount.unaryPlus(): Int =
        level() * configuration.indentationSize

    /**
     * @receiver the indentation amount.
     * @return the negated indentation level (in space characters).
     * @see IndentationAmount.unaryPlus
     */
    operator fun IndentationAmount.unaryMinus(): Int =
        -(+this)

    companion object Factory {
        /**
         * Creates a new instance.
         *
         * While you may call this function directly, consider using
         * [withIndentationConfig] instead.
         *
         * @param configuration the configuration this instance will wrap.
         * @return the newly created instance.
         * @see withIndentationConfig
         */
        operator fun invoke(configuration: IndentationConfig): IndentationConfigAware =
            object : IndentationConfigAware {
                override val configuration = configuration
            }

        /**
         * Calls the specified function [block] with [IndentationConfigAware] as
         * its receiver and returns its result.
         *
         * @param configuration the configuration for the indentation rule.
         * @param block the function block to call.
         * @return the result returned by the function block.
         */
        inline fun <T> withIndentationConfig(configuration: IndentationConfig,
                                             block: IndentationConfigAware.() -> T): T =
            with(IndentationConfigAware(configuration), block)
    }
}
