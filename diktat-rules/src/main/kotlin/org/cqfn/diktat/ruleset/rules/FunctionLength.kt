package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_LONG_FUNCTION
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule 5.1.1 check function length
 */
class FunctionLength(private val configRules: List<RulesConfig>) : Rule("function-length") {

    companion object {
        val FUNCTION_ALLOW_COMMENT = listOf(EOL_COMMENT, KDOC, BLOCK_COMMENT)
        private const val MAX_FUNCTION_LENGTH = 30L
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = FunctionLengthConfiguration(
                configRules.getRuleConfig(TOO_LONG_FUNCTION)?.configuration ?: mapOf()
        )

        if (node.elementType == FUN)
            checkFun(node.copyElement(), configuration.maxFunctionLength)
    }

    private fun checkFun(node: ASTNode, maxFunctionLength: Long) {
        node.findAllNodesWithCondition { it.elementType in FUNCTION_ALLOW_COMMENT }.forEach { it.treeParent.removeChild(it) }
        val functionText = node.text.lines().filter { it.isNotBlankAndEmpty() }
        if (functionText.size > maxFunctionLength)
            TOO_LONG_FUNCTION.warn(configRules,emitWarn, isFixMode,
                    "max length is $maxFunctionLength, but you have ${functionText.size}", node.startOffset, node)
    }

    class FunctionLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxFunctionLength = config["maxFunctionLength"]?.toLong() ?: MAX_FUNCTION_LENGTH
    }
}
