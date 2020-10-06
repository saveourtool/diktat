package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.ruleset.rules.files.IndentationError
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace

internal abstract class CustomIndentationChecker(protected val configuration: IndentationConfig) {
    /**
     * This method checks if this white space is an exception from general rule
     * If true, checks if it is properly indented and fixes
     * @return null true if node is not an exception, CheckResult otherwise
     */
    abstract fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult?
}

/**
 * @property adjustNext Indicates whether the indent returned by this exception checker needs to be applied to other nodes with same parent
 * @property includeLastChild Indicates whether the white space before the last child node of the initiator node should have increased indent or not
 */
internal data class CheckResult(val isCorrect: Boolean, val expectedIndent: Int, val adjustNext: Boolean, val includeLastChild: Boolean = true) {
    companion object {
        fun from(actual: Int, expected: Int, adjustNext: Boolean = false, includeLastChild: Boolean = true) =
                CheckResult(actual == expected, expected, adjustNext, includeLastChild)
    }
}
