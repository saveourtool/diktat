package org.cqfn.diktat.api

/**
 * Error found by `diktat`
 */
interface DiktatError {
    /**
     * @return line number (one-based)
     */
    fun getLine(): Int

    /**
     * @return column number (one-based)
     */
    fun getCol(): Int

    /**
     * @return rule id
     */
    fun getRuleId(): String

    /**
     * error message
     */
    fun getDetail(): String

    /**
     * @return true if the found error can be fixed
     */
    fun canBeAutoCorrected(): Boolean
}
