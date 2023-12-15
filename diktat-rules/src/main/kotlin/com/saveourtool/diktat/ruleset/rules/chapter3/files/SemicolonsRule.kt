package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.REDUNDANT_SEMICOLON
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.extractLineOfText
import com.saveourtool.diktat.ruleset.utils.isEol
import org.jetbrains.kotlin.KtNodeTypes.ENUM_ENTRY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.SEMICOLON

/**
 * Rule that checks usage of semicolons at the end of line
 */
@Suppress("ForbiddenComment")
class SemicolonsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(REDUNDANT_SEMICOLON)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == SEMICOLON) {
            handleSemicolon(node)
        }
    }

    /**
     * Check that EOL semicolon is used only in enums
     */
    private fun handleSemicolon(node: ASTNode) {
        if (node.isEol() && node.treeParent.elementType != ENUM_ENTRY) {
            // semicolon at the end of line which is not part of enum members declarations
            REDUNDANT_SEMICOLON.warnAndFix(configRules, emitWarn, isFixMode, node.extractLineOfText(), node.startOffset, node) {
                node.treeParent.removeChild(node)
            }
        }
    }

    companion object {
        const val NAME_ID = "semicolon"
    }
}
