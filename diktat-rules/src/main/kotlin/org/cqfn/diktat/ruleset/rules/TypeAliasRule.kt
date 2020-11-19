package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.LT
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPEALIAS
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.TYPE_ALIAS
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks if variable has long type reference and two or more nested generics.
 * Length type reference can be configured
 */
class TypeAliasRule(private val configRules: List<RulesConfig>) : Rule("type-alias") {

    companion object {
        const val TYPE_REFERENCE_MAX_LENGTH = 25
    }

    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == TYPE_REFERENCE && node.parents().map { it.elementType }.none { it == SUPER_TYPE_LIST  ||it == TYPEALIAS}) {
            checkTypeReference(node, TypeAliasConfiguration(configRules.getRuleConfig(TYPE_ALIAS)?.configuration ?: mapOf()))
        }
    }

    /**
     * Check properties for nested generics. Count LT for generic types and VALUE_PARAMETER for functional types
     */
    private fun checkTypeReference(node: ASTNode, config: TypeAliasConfiguration) {
        if (node.textLength > config.typeReferenceLength)
            if (node.findAllNodesWithSpecificType(LT).size > 1 || node.findAllNodesWithSpecificType(VALUE_PARAMETER).size > 1)
                TYPE_ALIAS.warn(configRules, emitWarn, isFixMode, "too long type reference", node.startOffset, node)
    }

    class TypeAliasConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val typeReferenceLength = config["typeReferenceLength"]?.toIntOrNull() ?: TYPE_REFERENCE_MAX_LENGTH
    }
}
