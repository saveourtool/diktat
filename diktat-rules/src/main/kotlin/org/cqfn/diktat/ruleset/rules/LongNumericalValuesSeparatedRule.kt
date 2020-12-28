package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_LITERAL
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

import java.lang.StringBuilder

/**
 * Rule that checks if numerical separators (`_`) are used for long numerical literals
 */
class LongNumericalValuesSeparatedRule(private val configRules: List<RulesConfig>) : Rule("long-numerical-values") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = LongNumericalValuesConfiguration(
            configRules.getRuleConfig(Warnings.LONG_NUMERICAL_VALUES_SEPARATED)?.configuration ?: emptyMap())

        if (node.elementType == INTEGER_LITERAL) {
            if (!isValidConstant(node.text, configuration, node)) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                    fixIntegerConstant(node, configuration.maxBlockLength)
                }
            }
        }

        if (node.elementType == FLOAT_LITERAL) {
            if (!isValidConstant(node.text, configuration, node)) {
                val parts = node.text.split(".")

                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                    fixFloatConstantPart(parts[0], parts[1], configuration, node)
                }
            }
        }
    }

    private fun fixIntegerConstant(node: ASTNode, maxBlockLength: Int) {
        val resultRealPart = StringBuilder(nodePrefix(node.text))

        val chunks = removePrefixSuffix(node.text)
            .reversed()
            .chunked(maxBlockLength)
            .reversed()
        resultRealPart.append(chunks.joinToString(separator = "_") { it.reversed() })

        resultRealPart.append(nodeSuffix(node.text))
        (node as LeafPsiElement).replaceWithText(resultRealPart.toString())
    }

    private fun fixFloatConstantPart(
        realPart: String,
        fractionalPart: String,
        configuration: LongNumericalValuesConfiguration,
        node: ASTNode) {
        val resultRealPart = StringBuilder(nodePrefix(realPart))
        val resultFractionalPart = StringBuilder()

        val realNumber = removePrefixSuffix(realPart)
        if (realNumber.length > configuration.maxLength) {
            val chunks = realNumber
                .reversed()
                .chunked(configuration.maxBlockLength)
                .reversed()
            resultRealPart.append(chunks.joinToString(separator = "_") { it.reversed() })

            resultRealPart.append(nodeSuffix(realPart)).append(".")
        } else {
            resultRealPart.append(realNumber).append(".")
        }

        val fractionalNumber = removePrefixSuffix(fractionalPart)
        if (fractionalNumber.length > configuration.maxLength) {
            val chunks = fractionalNumber.chunked(configuration.maxBlockLength)
            resultFractionalPart.append(chunks.joinToString(separator = "_", postfix = nodeSuffix(fractionalPart)) { it })

            resultFractionalPart.append(nodeSuffix(fractionalPart))
        } else {
            resultFractionalPart.append(fractionalNumber).append(nodeSuffix(fractionalPart))
        }

        (node as LeafPsiElement).replaceWithText(resultRealPart.append(resultFractionalPart).toString())
    }

    private fun nodePrefix(nodeText: String) = when {
        nodeText.startsWith("0b") -> "0b"
        nodeText.startsWith("0x") -> "0x"
        else -> ""
    }

    private fun nodeSuffix(nodeText: String) = when {
        nodeText.endsWith("L") -> "L"
        nodeText.endsWith("f", true) -> "f"
        else -> ""
    }

    private fun isValidConstant(
        text: String,
        configuration: LongNumericalValuesConfiguration,
        node: ASTNode): Boolean {
        if (text.contains("_")) {
            checkBlocks(removePrefixSuffix(text), configuration, node)
            return true
        }

        return text
            .split(".")
            .map { removePrefixSuffix(it) }
            .all { it.length <= configuration.maxLength }
    }

    private fun checkBlocks(
        text: String,
        configuration: LongNumericalValuesConfiguration,
        node: ASTNode) {
        val blocks = text.split("_", ".")

        blocks.forEach {
            if (it.length > configuration.maxBlockLength) {
                Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warn(configRules, emitWarn, false, "this block is too long $it", node.startOffset, node)
            }
        }
    }

    private fun removePrefixSuffix(text: String): String {
        if (text.startsWith("0x")) {
            return text.removePrefix("0x")
        }

        return text.removePrefix("0b")
            .removeSuffix("L")
            .removeSuffix("f")
            .removeSuffix("F")
    }

    /**
     * [RuleConfiguration] for numerical literals separation
     */
    class LongNumericalValuesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum number of digits which are not split
         */
        val maxLength = config["maxNumberLength"]?.toIntOrNull() ?: MAX_NUMBER_LENGTH

        /**
         * Maximum number of digits between separators
         */
        val maxBlockLength = config["maxBlockLength"]?.toIntOrNull() ?: DELIMITER_LENGTH
    }

    companion object {
        private const val DELIMITER_LENGTH: Int = 3
        private const val MAX_NUMBER_LENGTH: Int = 3
    }
}
