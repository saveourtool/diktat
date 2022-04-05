package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.CUSTOM_GETTERS_SETTERS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.GET_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.core.ast.ElementType.SET_KEYWORD
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Inspection that checks that no custom getters and setters are used for properties.
 */
class CustomGetterSetterRule(configRules: List<RulesConfig>) : DiktatRule(
    "020-custom-getter-setter",
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

        setter?.let {
            // only private custom setters are allowed
            if (!isPrivateSetter) {
                CUSTOM_GETTERS_SETTERS.warn(configRules, emitWarn, isFixMode, setter.text, setter.startOffset, node)
            }
        }

        getter?.let {
            CUSTOM_GETTERS_SETTERS.warn(configRules, emitWarn, isFixMode, getter.text, getter.startOffset, node)
        }
    }
}
