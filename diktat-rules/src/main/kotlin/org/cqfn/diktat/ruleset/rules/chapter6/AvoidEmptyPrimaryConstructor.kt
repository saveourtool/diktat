package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_PRIMARY_CONSTRUCTOR
import org.cqfn.diktat.ruleset.rules.DiktatRule

import com.pinterest.ktlint.core.ast.ElementType.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

/**
 * This rule checks if a class has an empty primary constructor.
 */
class AvoidEmptyPrimaryConstructor(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(EMPTY_PRIMARY_CONSTRUCTOR)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS) {
            checkClass(node.psi as KtClass)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkClass(ktClass: KtClass) {
        if (ktClass.primaryConstructor?.valueParameters?.isNotEmpty() != false || ktClass.primaryConstructorModifierList != null) {
            return
        }
        EMPTY_PRIMARY_CONSTRUCTOR.warnAndFix(configRules, emitWarn, isFixMode, ktClass.nameIdentifier!!.text,
            ktClass.node.startOffset, ktClass.node) {
            ktClass.node.removeChild(ktClass.primaryConstructor!!.node)
        }
    }

    companion object {
        const val NAME_ID = "aao-avoid-empty-primary-constructor"
    }
}
