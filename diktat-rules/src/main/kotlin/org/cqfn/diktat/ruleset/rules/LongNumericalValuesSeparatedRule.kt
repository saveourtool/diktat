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

        if (node.elementType == INTEGER_LITERAL) {
            if(!isValidIntegerConstant(node.text, configuration, node)) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    fixIntegerConstant(node, configuration.maxBlockLength)
                }
            }
        }

        if (node.elementType == FLOAT_LITERAL) {
            if (node.text.contains("_")) {
                checkBlocks(removePrefixSuffix(node.text), configuration, node)
                return
            }

            val parts = node.text.split(".")

            if (removePrefixSuffix(parts[0]).length > configuration.maxLength ||
                    removePrefixSuffix(parts[1]).length > configuration.maxLength) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    fixFloatConstantPart(parts[0], parts[1], configuration, node)
                }
            }
        }

    }


    private fun fixIntegerConstant(node: ASTNode, maxBlockLength: Int) {
        val resultRealPart = StringBuilder(nodePrefix(node.text))

        val chunks = removePrefixSuffix(node.text).reversed().chunked(maxBlockLength).reversed()
        resultRealPart.append(chunks.joinToString(separator = "_") { it.reversed() })

        resultRealPart.append(nodeSuffix(node.text))
        (node as LeafPsiElement).replaceWithText(resultRealPart.toString())
    }

    private fun fixFloatConstantPart(realPart: String, fractionalPart: String, configuration: LongNumericalValuesConfiguration, node: ASTNode) {
        val resultRealPart = StringBuilder(nodePrefix(realPart))
        val resultFractionalPart = StringBuilder()

        if (removePrefixSuffix(realPart).length > configuration.maxLength) {
            val chunks = removePrefixSuffix(realPart).reversed().chunked(configuration.maxBlockLength).reversed()
            resultRealPart.append(chunks.joinToString(separator = "_") { it.reversed() })

            resultRealPart.append(nodeSuffix(realPart)).append(".")
        } else {
            resultRealPart.append(removePrefixSuffix(realPart)).append(".")
        }

        if ( removePrefixSuffix(fractionalPart).length > configuration.maxLength) {
            val chunks = removePrefixSuffix(fractionalPart).reversed().chunked(configuration.maxBlockLength).reversed()
            resultFractionalPart.append(chunks.joinToString(separator = "_", postfix = nodeSuffix(fractionalPart)) { it.reversed() })

            resultFractionalPart.append(nodeSuffix(fractionalPart))
        }

        (node as LeafPsiElement).replaceWithText(resultRealPart.append(resultFractionalPart).toString())
    }

    private fun nodePrefix(nodeText: String) : String {
        if (nodeText.startsWith("0b"))
            return "0b"

        if(nodeText.startsWith("0x"))
            return "0x"

        return ""
    }

    private fun nodeSuffix(nodeText: String) : String {
        if (nodeText.endsWith("L"))
            return "L"

        if (nodeText.endsWith("f", true))
            return "f"

        return ""
    }


    private fun isValidIntegerConstant (text: String, configuration: LongNumericalValuesConfiguration, node: ASTNode) : Boolean {
        if (text.contains("_")) {
            checkBlocks(removePrefixSuffix(text), configuration, node)
            return true
        }

        return removePrefixSuffix(text).length < configuration.maxLength
    }

    private fun checkBlocks(text: String, configuration: LongNumericalValuesConfiguration, node: ASTNode) {
        val blocks = text.split("_", ".")

        blocks.forEach {
            if (it.length > configuration.maxBlockLength) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warn(configRules, emitWarn, false, "this block is too long $it", node.startOffset)
            }
        }
    }

    private fun removePrefixSuffix (text : String) : String {
        val result = text.removePrefix("0b").
                removePrefix("0x").
                removeSuffix("L")

        if (result.length > 1) {
            return result.removeSuffix("f")
                    .removeSuffix("F")
        }

        return result
    }

    class LongNumericalValuesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxLength = config["maxNumberLength"]?.toIntOrNull() ?: MAX_NUMBER_LENGTH
        val maxBlockLength = config["maxBlockLength"]?.toIntOrNull() ?: DELIMITER_LENGTH
    }
}
