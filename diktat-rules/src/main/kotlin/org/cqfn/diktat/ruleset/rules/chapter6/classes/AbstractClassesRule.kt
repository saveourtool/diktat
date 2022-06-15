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
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_CALL_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * Checks if abstract class has any abstract method. If not, warns that class should not be abstract
 */
class AbstractClassesRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(CLASS_SHOULD_NOT_BE_ABSTRACT)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS) {
            val classBody = node.getFirstChildWithType(CLASS_BODY) ?: return

            // If an abstract class extends another class, than that base class can be abstract too.
            // Then this class must have `abstract` modifier even if it doesn't have any abstract members.
            // Class also can have `abstract` modifier if it implements interface
            if (node.hasAbstractModifier() && node.isNotSubclass()) {
                handleAbstractClass(classBody, node)
            }
        }
    }

    private fun ASTNode.hasAbstractModifier(): Boolean =
        getFirstChildWithType(MODIFIER_LIST)?.hasChildOfType(ABSTRACT_KEYWORD) ?: false

    private fun ASTNode.isNotSubclass(): Boolean = findChildByType(SUPER_TYPE_LIST)?.children()?.filter {
        it.elementType == SUPER_TYPE_CALL_ENTRY || it.elementType == SUPER_TYPE_ENTRY
    }?.none() ?: true

    @Suppress("UnsafeCallOnNullableType")
    private fun handleAbstractClass(node: ASTNode, classNode: ASTNode) {
        val functions = node.getAllChildrenWithType(FUN)
        val properties = node.getAllChildrenWithType(PROPERTY)
        val members = functions + properties

        val identifier = classNode.getFirstChildWithType(IDENTIFIER)!!.text

        if (members.isNotEmpty() && members.none { it.hasAbstractModifier() }) {
            CLASS_SHOULD_NOT_BE_ABSTRACT.warnAndFix(configRules, emitWarn, isFixMode, identifier, node.startOffset, node) {
                val modList = classNode.getFirstChildWithType(MODIFIER_LIST)!!
                val abstractKeyword = modList.getFirstChildWithType(ABSTRACT_KEYWORD)!!
                val newOpenKeyword = LeafPsiElement(OPEN_KEYWORD, "open")
                modList.replaceChild(abstractKeyword, newOpenKeyword)
            }
        }
    }

    companion object {
        const val NAME_ID = "abg-abstract-classes"
    }
}
