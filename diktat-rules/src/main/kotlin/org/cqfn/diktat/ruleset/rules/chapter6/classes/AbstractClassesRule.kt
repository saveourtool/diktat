package org.cqfn.diktat.ruleset.rules.chapter6.classes

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.ast.ElementType.ABSTRACT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OPEN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Checks if abstract class has any abstract method. If not, warns that class should not be abstract
 */
class AbstractClassesRule(configRules: List<RulesConfig>) : DiktatRule(
    "abstract-classes",
    configRules,
    listOf(CLASS_SHOULD_NOT_BE_ABSTRACT)) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS) {
            val classBody = node.getFirstChildWithType(CLASS_BODY) ?: return

            if (hasAbstractModifier(node)) {
                handleAbstractClass(classBody, node)
            }
        }
    }

    private fun hasAbstractModifier(node: ASTNode): Boolean =
            node.getFirstChildWithType(MODIFIER_LIST)?.hasChildOfType(ABSTRACT_KEYWORD) ?: false

    @Suppress("UnsafeCallOnNullableType")
    private fun handleAbstractClass(node: ASTNode, classNode: ASTNode) {
        val functions = node.getAllChildrenWithType(FUN)
        val properties = node.getAllChildrenWithType(PROPERTY)
        val members = functions + properties

        val identifier = classNode.getFirstChildWithType(IDENTIFIER)!!.text

        if (members.isNotEmpty() && members.none { hasAbstractModifier(it) }) {
            CLASS_SHOULD_NOT_BE_ABSTRACT.warnAndFix(configRules, emitWarn, isFixMode, identifier, node.startOffset, node) {
                val modList = classNode.getFirstChildWithType(MODIFIER_LIST)!!
                val abstractKeyword = modList.getFirstChildWithType(ABSTRACT_KEYWORD)!!
                val newOpenKeyword = LeafPsiElement(OPEN_KEYWORD, "open")
                modList.replaceChild(abstractKeyword, newOpenKeyword)
            }
        }
    }
}
