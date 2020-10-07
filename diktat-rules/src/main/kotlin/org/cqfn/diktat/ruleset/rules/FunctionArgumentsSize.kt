package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_PARAMETERS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

class FunctionArgumentsSize(private val configRules: List<RulesConfig>) : Rule("argument-size") {

    companion object {
        const val maxDefaultParameterSize = 5L
    }

    private val configuration: FunctionArgumentsSizeConfiguration by lazy {
        FunctionArgumentsSizeConfiguration(configRules.getRuleConfig(TOO_MANY_PARAMETERS)?.configuration ?: mapOf())
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.FUN) {
            checkFun(node, configuration.maxParameterSize)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(node: ASTNode, maxParameterSize: Long) {
        val parameterListSize = (node.psi as KtFunction).valueParameters.size
        if (parameterListSize > maxParameterSize){
            TOO_MANY_PARAMETERS.warn(configRules, emitWarn, isFixMode,
                    "${node.findChildByType(IDENTIFIER)!!.text} has $parameterListSize, but allowed $maxParameterSize", node.startOffset, node)
        }
    }

    class FunctionArgumentsSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxParameterSize = config["maxParameterListSize"]?.toLongOrNull()?: maxDefaultParameterSize
    }
}
