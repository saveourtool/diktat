package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
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
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_USING_UTILITY_CLASS
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * Rule 6.4.1 checks that class/object, with a word "util" in its name, has only functions.
 */
class AvoidUtilityClass(private val configRules: List<RulesConfig>) : Rule("avoid-utility-class") {

    companion object {
        private val UTILITY_CLASS_CHILDREN = listOf(LBRACE, WHITE_SPACE, FUN, RBRACE, KDOC,
                EOL_COMMENT, BLOCK_COMMENT, OBJECT_DECLARATION)
    }

    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect
        if (node.elementType == OBJECT_DECLARATION || node.elementType == CLASS ) {
            checkClass(node)
        }
    }

    private fun checkClass(node: ASTNode) {
        if (node.hasChildOfType(PRIMARY_CONSTRUCTOR)
                || node.findChildByType(IDENTIFIER)?.text?.toLowerCase()?.contains("util") == false) return
        node.findChildByType(CLASS_BODY)
                ?.children()
                ?.toList()
                ?.takeIf { childList -> childList.all { it.elementType in UTILITY_CLASS_CHILDREN } }
                ?.filter { it.elementType == FUN }
                ?.ifEmpty { return }
                ?: return
        AVOID_USING_UTILITY_CLASS.warn(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)!!.text, node.startOffset, node)
    }
}
