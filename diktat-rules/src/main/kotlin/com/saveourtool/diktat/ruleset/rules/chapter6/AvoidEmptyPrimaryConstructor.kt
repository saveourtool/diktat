package com.saveourtool.diktat.ruleset.rules.chapter6

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.EMPTY_PRIMARY_CONSTRUCTOR
import com.saveourtool.diktat.ruleset.rules.DiktatRule

import org.jetbrains.kotlin.KtNodeTypes.CLASS
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
        const val NAME_ID = "avoid-empty-primary-constructor"
    }
}
