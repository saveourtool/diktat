package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_PARAMETERS

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

class FunctionArgumentsSize(private val configRules: List<RulesConfig>) : Rule("argument-size") {
    private var isFixMode: Boolean = false
    private val configuration: FunctionArgumentsSizeConfiguration by lazy {
        FunctionArgumentsSizeConfiguration(configRules.getRuleConfig(TOO_MANY_PARAMETERS)?.configuration ?: mapOf())
    }
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.FUN) {
            checkFun(node, configuration.maxParameterSize)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(node: ASTNode, maxParameterSize: Long) {
        val parameterListSize = (node.psi as KtFunction).valueParameters.size
        if (parameterListSize > maxParameterSize) {
            TOO_MANY_PARAMETERS.warn(configRules, emitWarn, isFixMode,
                "${node.findChildByType(IDENTIFIER)!!.text} has $parameterListSize, but allowed $maxParameterSize", node.startOffset, node)
        }
    }

    class FunctionArgumentsSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxParameterSize = config["maxParameterListSize"]?.toLongOrNull() ?: MAX_DEFAULT_PARAMETER_SIZE
    }

    companion object {
        const val MAX_DEFAULT_PARAMETER_SIZE = 5L
    }
}
