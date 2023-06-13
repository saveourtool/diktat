package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_MANY_BLANK_LINES
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.SCRIPT
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * This rule checks usage of blank lines in code.
 * 1. Checks that no more than two consecutive blank lines are used in a row
 * 2. Checks that blank lines are not put in the beginning or at the end of code blocks with curly braces
 */
class BlankLinesRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(TOO_MANY_BLANK_LINES),
) {
    override fun logic(node: ASTNode) {
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
        if (node.treeParent.let {
            // kts files are parsed as a SCRIPT node containing BLOCK, therefore WHITE_SPACEs from these BLOCKS shouldn't be checked
            it.elementType == BLOCK && it.treeParent?.elementType != SCRIPT ||
                    it.elementType == CLASS_BODY || it.elementType == FUNCTION_LITERAL
        }) {
            node.findParentNodeWithSpecificType(LAMBDA_ARGUMENT)?.let {
                // Lambda body is always has a BLOCK -> run { } - (LBRACE, WHITE_SPACE, BLOCK "", RBRACE)
                if (node.treeNext.text.isEmpty()) {
                    return
                }
            }

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
            if (node.treeParent.elementType != KtFileElementType.INSTANCE && (node.treeParent.getFirstChildWithType(WHITE_SPACE) == node ||
                    node.treeParent.getAllChildrenWithType(WHITE_SPACE).last() == node)) {
                node.leaveExactlyNumNewLines(1)
            } else {
                node.leaveExactlyNumNewLines(2)
            }
        }
    }

    companion object {
        const val NAME_ID = "blank-lines"
    }
}
