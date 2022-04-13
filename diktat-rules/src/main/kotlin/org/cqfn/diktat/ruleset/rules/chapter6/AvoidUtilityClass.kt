package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_USING_UTILITY_CLASS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
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
        AVOID_USING_UTILITY_CLASS.warn(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)?.text ?: node.text, node.startOffset, node)
    }

    companion object {
        const val NAME_ID = "abe-avoid-utility-class"
        private val utilityClassChildren = listOf(LBRACE, WHITE_SPACE, FUN, RBRACE, KDOC,
            EOL_COMMENT, BLOCK_COMMENT, OBJECT_DECLARATION)
    }
}
