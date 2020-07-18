package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.ruleset.rules.files.IndentationRule.IndentationError
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace

internal abstract class CustomIndentationChecker(protected val configuration: IndentationConfig) {
    /**
     * This method checks if this white space is an exception from general rule
     * If true, checks if it is properly indented and fixes
     * @return null true if node is not an exception, CheckResult otherwise
     */
    abstract fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult?
}

internal data class CheckResult(val correct: Boolean, val expected: Int)
