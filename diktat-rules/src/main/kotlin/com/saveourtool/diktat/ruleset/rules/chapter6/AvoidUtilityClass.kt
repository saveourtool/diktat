package com.saveourtool.diktat.ruleset.rules.chapter6

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.ruleset.constants.Warnings.AVOID_USING_UTILITY_CLASS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.psiUtil.children

import java.util.Locale

/**
 * Rule 6.4.1 checks that class/object, with a word "util" in its name, has only functions.
 */
class AvoidUtilityClass(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(AVOID_USING_UTILITY_CLASS)
) {
    override fun logic(node: ASTNode) {
        val config = configRules.getCommonConfiguration()
        val filePath = node.getFilePath()
        if (!node.hasTestAnnotation() && !isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors)) {
            @Suppress("COLLAPSE_IF_STATEMENTS")
            if (node.elementType == OBJECT_DECLARATION || node.elementType == CLASS) {
                checkClass(node)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "WRONG_NEWLINES")
    private fun checkClass(node: ASTNode) {
        // checks that class/object doesn't contain primary constructor and its identifier doesn't has "utli"
        if (!node.hasChildOfType(IDENTIFIER) || node.hasChildOfType(PRIMARY_CONSTRUCTOR) ||
                !node.findChildByType(IDENTIFIER)!!.text.lowercase(Locale.getDefault()).contains("util")) {
            return
        }
        node.findChildByType(CLASS_BODY)
            ?.children()
            ?.toList()
            ?.takeIf { childList -> childList.all { it.elementType in utilityClassChildren } }
            ?.filter { it.elementType == FUN }
            ?.ifEmpty { return }
            ?: return
        AVOID_USING_UTILITY_CLASS.warn(configRules, emitWarn, node.findChildByType(IDENTIFIER)?.text ?: node.text, node.startOffset, node)
    }

    companion object {
        const val NAME_ID = "avoid-utility-class"
        private val utilityClassChildren = listOf(LBRACE, WHITE_SPACE, FUN, RBRACE, KDOC,
            EOL_COMMENT, BLOCK_COMMENT, OBJECT_DECLARATION)
    }
}
