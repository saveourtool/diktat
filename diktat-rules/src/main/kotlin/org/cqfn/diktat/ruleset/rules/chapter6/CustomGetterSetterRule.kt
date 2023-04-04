package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.CUSTOM_GETTERS_SETTERS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.GET_KEYWORD
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.OVERRIDE_KEYWORD
import org.jetbrains.kotlin.KtNodeTypes.PRIVATE_KEYWORD
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY_ACCESSOR
import org.jetbrains.kotlin.KtNodeTypes.SET_KEYWORD
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Inspection that checks that no custom getters and setters are used for properties.
 */
class CustomGetterSetterRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(CUSTOM_GETTERS_SETTERS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == PROPERTY_ACCESSOR) {
            checkForCustomGetersSetters(node)
        }
    }

    private fun checkForCustomGetersSetters(node: ASTNode) {
        val getter = node.getFirstChildWithType(GET_KEYWORD)
        val setter = node.getFirstChildWithType(SET_KEYWORD)
        val isPrivateSetter = node.getFirstChildWithType(MODIFIER_LIST)?.hasAnyChildOfTypes(PRIVATE_KEYWORD) ?: false
        val isOverrideGetter = node.treeParent.getFirstChildWithType(MODIFIER_LIST)?.hasAnyChildOfTypes(OVERRIDE_KEYWORD) ?: false

        setter?.let {
            // only private custom setters are allowed
            if (!isPrivateSetter) {
                CUSTOM_GETTERS_SETTERS.warn(configRules, emitWarn, isFixMode, setter.text, setter.startOffset, node)
            }
        }

        getter?.let {
            // only override getter are allowed
            if (!isOverrideGetter) {
                CUSTOM_GETTERS_SETTERS.warn(configRules, emitWarn, isFixMode, getter.text, getter.startOffset, node)
            }
        }
    }

    companion object {
        const val NAME_ID = "custom-getter-setter"
    }
}
