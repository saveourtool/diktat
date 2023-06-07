package com.saveourtool.diktat.ruleset.rules.chapter3.files

/**
 * A contract for types which encapsulate the indentation level.
 */
internal interface IndentationAware {
    /**
     * @return the indentation (the amount of space characters) of this element.
     */
    val indentation: Int
}
