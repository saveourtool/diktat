package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_LITERAL
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import java.lang.StringBuilder

class LongNumericalValuesSeparatedRule : Rule("long-numerical-values") {
    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    companion object {
        private const val DELIMITER_LENGTH: Int = 3
        private const val MAX_NUMBER_LENGTH: Int = 3
    }

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = LongNumericalValuesConfiguration(
                configRules.getRuleConfig(Warnings.LONG_NUMERICAL_VALUES_SEPARATED)?.configuration ?: mapOf())

        if (node.elementType == INTEGER_LITERAL || node.elementType == FLOAT_LITERAL) {
            if(!isValidConstant(node.text, configuration, node)) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    fixConstant(node, configuration)
                }
            }
        }

    }

    private fun fixConstant(node: ASTNode, configuration: LongNumericalValuesConfiguration) {
        val parts = node.text.split(".")
        val realPart = removePrefixSuffix(parts[0])

        val resultRealPart = StringBuilder(nodePrefix(node.text))

        // We can get here in 2 cases:
        // 1. When integer number is > maxLength
        // 2. When float number realPart > maxLength
        if (realPart.length > configuration.maxLength) {
            val chunks = realPart.reversed().chunked(DELIMITER_LENGTH).reversed()
            chunks.forEach {
                resultRealPart.append(it.reversed()).append("_")
            }
            resultRealPart.deleteCharAt(resultRealPart.length - 1)
        } else {
            // Here we get in 1 case: when realPart of float number is < maxLength
            resultRealPart.append(realPart).append(".")
        }

        if (parts.size > 1 && parts[1].length > configuration.maxLength) {
            val resultFractionalPart = StringBuilder()
            val chunks = parts[1].reversed().chunked(DELIMITER_LENGTH).reversed()
            chunks.forEach {
                resultFractionalPart.append(it.reversed()).append("_")
            }
            resultFractionalPart.deleteCharAt(resultFractionalPart.length - 1)
            resultFractionalPart.append(nodeSuffix(node.text))

            (node as LeafPsiElement).replaceWithText(resultRealPart.append(resultFractionalPart).toString())
            return
        }

        resultRealPart.append(nodeSuffix(node.text))
        (node as LeafPsiElement).replaceWithText(resultRealPart.toString())
    }

    private fun nodePrefix(node: String) : String {
        if (node.contains("0b"))
            return "0b"

        if(node.contains("0x"))
            return "0x"

        return ""
    }

    private fun nodeSuffix(node: String) : String {
        if (node.contains("L"))
            return "L"

        if (node.contains("f"))
            return "f"

        if (node.contains("F"))
            return "F"

        return ""
    }


    private fun isValidConstant(text: String, configuration: LongNumericalValuesConfiguration, node: ASTNode) : Boolean {
        if (text.contains("_")) {
            val number = removePrefixSuffix(text)
            checkBlocks(number, configuration, node)
            return true
        }

        val parts = text.split(".")

        val realPart = removePrefixSuffix(parts[0])


        if (parts.size > 1) {
            return parts[1].length < configuration.maxLength
        }

        return realPart.length < configuration.maxLength
    }

    private fun checkBlocks(text: String, configuration: LongNumericalValuesConfiguration, node: ASTNode) {
        val blocks = text.split("_", ".")

        blocks.forEach {
            if (it.length > configuration.maxLength) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warn(configRules, emitWarn, isFixMode, "this block is too long $it", node.startOffset)
            }
        }
    }

    private fun removePrefixSuffix (text : String) : String {
        return text.removePrefix("0b").
            removePrefix("0x").
            removeSuffix("L").
            removeSuffix("f").
            removeSuffix("F")
    }

    class LongNumericalValuesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxLength = config["maxNumberLength"]?.toIntOrNull() ?: MAX_NUMBER_LENGTH
    }
}
