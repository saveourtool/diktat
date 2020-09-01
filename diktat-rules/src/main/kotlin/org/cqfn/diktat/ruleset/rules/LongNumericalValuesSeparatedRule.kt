package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class LongNumericalValuesSeparatedRule : Rule("long-numerical-values") {
    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = LongNumericalValuesConfiguration(
                configRules.getRuleConfig(Warnings.LONG_NUMERICAL_VALUES_SEPARATED)?.configuration ?: mapOf())

        if (node.elementType == INTEGER_CONSTANT || node.elementType == FLOAT_CONSTANT) {
            if(!isValidConstant(node.text, configuration)) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warn(configRules, emitWarn, isFixMode, "${node.text}", node.startOffset)
            }
        }

    }

    private fun isValidConstant(text: String, configuration: LongNumericalValuesConfiguration) : Boolean {
        if (text.contains("_"))
            return true

        val realPart = text.apply {
            removePrefix("0b")
            removePrefix("0x")
            removeSuffix("L")
            removeSuffix("f")
            removeSuffix("F")
        }

        val fractionalPart = text.split(".")

        if (fractionalPart.size > 1) {
            return fractionalPart[1].length < configuration.maxLength
        }

        return realPart.length < configuration.maxLength
    }

    class LongNumericalValuesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxLength = config["maxLength"]?.toLongOrNull() ?: 8
    }
}