package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.SCRIPT_INITIALIZER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement

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

        if (node.elementType == SCRIPT_INITIALIZER && node.getRootNode().getFilePath().isKotlinScript()) {
            checkNode(node)
        }
    }

    private fun checkNode(node: ASTNode) {
        if (!possibleWrapper.any { node.text.replace("\\s".toRegex(), "").startsWith(it) }) {
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

    companion object {
        private val possibleWrapper = listOf("tasks.register", "run{", "also{")
    }
}