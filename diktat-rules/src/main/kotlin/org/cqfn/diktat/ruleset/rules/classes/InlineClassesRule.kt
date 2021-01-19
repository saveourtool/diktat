package org.cqfn.diktat.ruleset.rules.classes

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.INLINE_CLASS_CAN_BE_USED
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FINAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PUBLIC_KEYWORD
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

class InlineClassesRule(private val configRule: List<RulesConfig>) : Rule("inline-classes") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(
            node: ASTNode,
            autoCorrect: Boolean,
            emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS) {
            handleClasses(node)
        }
    }

    private fun handleClasses(node: ASTNode) {
        if ((node.psi as KtClass).getProperties().size == 1 && !(node.psi as KtClass).hasExplicitPrimaryConstructor()) {
            val modList = node.getFirstChildWithType(MODIFIER_LIST)
            if (modList == null || modList.getChildren(null).all { it.elementType in goodModifiers }) {
                INLINE_CLASS_CAN_BE_USED.warnAndFix(configRule, emitWarn, isFixMode, "class ${(node.psi as KtClass).name}", node.startOffset, node) {
                    // Fixme: since it's an experimental feature we shouldn't do fixer
                }
            }
        }
    }

    companion object {
        val goodModifiers = listOf(PUBLIC_KEYWORD, PRIVATE_KEYWORD, FINAL_KEYWORD, PROTECTED_KEYWORD, INTERNAL_KEYWORD)
    }
}
