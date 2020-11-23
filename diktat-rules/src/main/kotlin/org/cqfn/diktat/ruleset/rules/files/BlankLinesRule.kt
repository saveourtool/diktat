package org.cqfn.diktat.ruleset.rules.files

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_BLANK_LINES
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.leaveExactlyNumNewLines
import org.cqfn.diktat.ruleset.utils.leaveOnlyOneNewLine
import org.cqfn.diktat.ruleset.utils.numNewLines
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This rule checks usage of blank lines in code.
 * 1. Checks that no more than two consecutive blank lines are used in a row
 * 2. Checks that blank lines are not put in the beginning or at the end of code blocks with curly braces
 */
class BlankLinesRule(private val configRules: List<RulesConfig>) : Rule("blank-lines") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == WHITE_SPACE) {
            // note that no blank lines counts as one newline
            if (node.numNewLines() == 2) {
                handleBlankLine(node)
            } else if (node.numNewLines() > 2) {
                handleTooManyBlankLines(node)
            }
        }
    }

    private fun handleBlankLine(node: ASTNode) {
        if (node.treeParent.elementType.let { it == BLOCK || it == CLASS_BODY || it == FUNCTION_LITERAL }) {
            if ((node.treeNext.elementType == RBRACE) xor (node.treePrev.elementType == LBRACE)) {
                // if both are present, this is not beginning or end
                // if both are null, then this block is empty and is handled in another rule
                val freeText = "do not put newlines ${if (node.treePrev.elementType == LBRACE) "in the beginning" else "at the end"} of code blocks"
                TOO_MANY_BLANK_LINES.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
                    node.leaveOnlyOneNewLine()
                }
            }
        }
    }

    private fun handleTooManyBlankLines(node: ASTNode) {
        TOO_MANY_BLANK_LINES.warnAndFix(configRules, emitWarn, isFixMode, "do not use more than two consecutive blank lines", node.startOffset, node) {
            if (node.treeParent.elementType != FILE && node.treeParent.getFirstChildWithType(WHITE_SPACE) == node)
                node.leaveExactlyNumNewLines(1)
            else
                node.leaveExactlyNumNewLines(2)
        }
    }
}
