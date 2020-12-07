package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.GET_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.core.ast.ElementType.SET_KEYWORD
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Inspection that checks that no custom getters and setters are used for properties.
 */
class CustomGetterSetterRule(private val configRules: List<RulesConfig>) : Rule("custom-getter-setter") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

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
                Warnings.CUSTOM_GETTERS_SETTERS.warn(configRules, emitWarn, isFixMode, setter.text, setter.startOffset, node)
            }
        }

        getter?.let {
            Warnings.CUSTOM_GETTERS_SETTERS.warn(configRules, emitWarn, isFixMode, getter.text, getter.startOffset, node)
        }
    }
}
