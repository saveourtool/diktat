package com.saveourtool.diktat.ruleset.rules.chapter4

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TYPE_ALIAS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.SUPER_TYPE_LIST
import org.jetbrains.kotlin.KtNodeTypes.TYPEALIAS
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.LT
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks if variable has long type reference and two or more nested generics.
 * Length type reference can be configured
 */
class TypeAliasRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(TYPE_ALIAS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == TYPE_REFERENCE && node
            .parents()
            .map { it.elementType }
            .none { it == SUPER_TYPE_LIST || it == TYPEALIAS }) {
            checkTypeReference(node, TypeAliasConfiguration(configRules.getRuleConfig(TYPE_ALIAS)?.configuration ?: emptyMap()))
        }
    }

    /**
     * Check properties for nested generics. Count LT for generic types and VALUE_PARAMETER for functional types
     */
    private fun checkTypeReference(node: ASTNode, config: TypeAliasConfiguration) {
        if (node.textLength > config.typeReferenceLength) {
            @Suppress("COLLAPSE_IF_STATEMENTS")
            if (node.findAllDescendantsWithSpecificType(LT).size > 1 || node.findAllDescendantsWithSpecificType(VALUE_PARAMETER).size > 1) {
                TYPE_ALIAS.warn(configRules, emitWarn, "too long type reference", node.startOffset, node)
            }
        }
    }

    /**
     * [RuleConfiguration] about using type aliases instead of complex types
     */
    class TypeAliasConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum length of a type before suggesting to use typealias
         */
        val typeReferenceLength = config["typeReferenceLength"]?.toIntOrNull() ?: TYPE_REFERENCE_MAX_LENGTH
    }

    companion object {
        const val NAME_ID = "type-alias"
        const val TYPE_REFERENCE_MAX_LENGTH = 25
    }
}
