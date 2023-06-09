package com.saveourtool.diktat.api

/**
 * Error found by `diktat`
 *
 * @property line line number (one-based)
 * @property col column number (one-based)
 * @property ruleId rule id
 * @property detail error message
 * @property canBeAutoCorrected true if the found error can be fixed
 */
data class DiktatError(
    val line: Int,
    val col: Int,
    val ruleId: String,
    val detail: String,
    val canBeAutoCorrected: Boolean = false,
)
