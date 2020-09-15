package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TYPE_ALIAS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class TypeAliasRule(private val configRules: List<RulesConfig>) : Rule("type-alias") {

    companion object {
        const val TYPE_REFERENCE_MAX_LENGTH = 25
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        val config = TypeAliasConfiguration(configRules.getRuleConfig(TYPE_ALIAS)?.configuration ?: mapOf())
        if (node.elementType == PROPERTY)
            checkProperty(node, config)
    }

    private fun checkProperty(node: ASTNode, config: TypeAliasConfiguration) {
        val typeReference = node.findChildByType(TYPE_REFERENCE) ?: return
        if (typeReference.textLength > config.typeReferenceLength)
            TYPE_ALIAS.warn(configRules, emitWarn, isFixMode, "too long type reference", typeReference.startOffset)
    }

    class TypeAliasConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val typeReferenceLength = config["typeReferenceLength"]?.toIntOrNull() ?: TYPE_REFERENCE_MAX_LENGTH
    }
}
