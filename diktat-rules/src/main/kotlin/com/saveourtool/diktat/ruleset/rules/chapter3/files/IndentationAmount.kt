package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig

/**
 * Encapsulates the change in the indentation level.
 */
@Suppress("WRONG_DECLARATIONS_ORDER")
internal enum class IndentationAmount {
    /**
     * The indent should be preserved at the current level.
     */
    NONE,

    /**
     * The indent should be increased or decreased by 1 (regular single indent).
     */
    SINGLE,

    /**
     * Extended, or _continuation_ indent. Applicable when any of
     * [`extendedIndent*`][IndentationConfig] flags is **on**.
     */
    EXTENDED,
    ;

    /**
     * @return the indentation level. To get the actual indentation (the amount
     *   of space characters), the value needs to be multiplied by
     *   [IndentationConfig.indentationSize].
     * @see IndentationConfig.indentationSize
     */
    fun level(): Int =
        ordinal

    /**
     * @return whether this amount represents the change in the indentation
     *   level, i.e. whether the element should be indented or un-indented.
     */
    fun isNonZero(): Boolean =
        level() > 0

    companion object {
        /**
         * A convenience factory method.
         *
         * @param extendedIndent the actual value of ony of the `extendedIndent*`
         *   flags.
         * @return the corresponding indentation amount, either [SINGLE] or
         *   [EXTENDED].
         */
        @JvmStatic
        fun valueOf(extendedIndent: Boolean): IndentationAmount =
            when {
                extendedIndent -> EXTENDED
                else -> SINGLE
            }
    }
}
