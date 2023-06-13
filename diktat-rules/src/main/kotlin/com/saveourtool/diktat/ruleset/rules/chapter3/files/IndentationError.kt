package com.saveourtool.diktat.ruleset.rules.chapter3.files

/**
 * @property expected expected indentation as a number of spaces
 * @property actual actual indentation as a number of spaces
 */
internal data class IndentationError(val expected: Int, val actual: Int)
