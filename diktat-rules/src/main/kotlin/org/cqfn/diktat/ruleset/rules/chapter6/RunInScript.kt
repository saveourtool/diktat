package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.SCRIPT_INITIALIZER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement

/**
 * Rule that checks if kts script contains other functions except run code
 */
class RunInScript(private val configRules: List<RulesConfig>) : Rule("run-script") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        isFixMode = autoCorrect
        emitWarn = emit

        val configuration = RunInScriptRuleConfiguration(
            configRules.getRuleConfig(RUN_IN_SCRIPT)?.configuration ?: emptyMap()
        )
        if (node.elementType == SCRIPT_INITIALIZER && node.getRootNode().getFilePath().isKotlinScript()) {
            checkNode(node, configuration)
        }
    }

    private fun checkNode(node: ASTNode, configuration: RunInScriptRuleConfiguration) {
        val possibleWrapperFromConfig = configuration.possibleWrapperConfig
            .takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
        if (!(possibleWrapper + possibleWrapperFromConfig)
            .any { node.text.replace("\\s".toRegex(), "").startsWith(it) }) {
            RUN_IN_SCRIPT.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                val parent = node.treeParent
                val newNode = KotlinParser().createNode("run {\n ${node.text}\n} \n")
                val newScript = CompositeElement(SCRIPT_INITIALIZER)
                parent.addChild(newScript, node)
                newScript.addChild(newNode)
                parent.removeChild(node)
            }
        }
    }

    /**
     * [RuleConfiguration] for possible wrapper
     */
    class RunInScriptRuleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Another possible wrapper that can be passed in config
         */
        val possibleWrapperConfig = config["possibleWrapper"] ?: ""
    }

    companion object {
        private val possibleWrapper = listOf("tasks.register", "run{", "also{", "repositories{", "plugins{", "diktat{")
    }
}
