package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

@Suppress("ForbiddenComment")
class WhenMustHaveElseRule : Rule("no-else-in-when") {

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.WHEN) {
            checkEntries(node)
        }
    }


    private fun checkEntries(node: ASTNode) {
        val hasElse = node.findAllNodesWithSpecificType(ElementType.WHEN_ENTRY).any {
            it.hasChildOfType(ElementType.ELSE_KEYWORD)
        }

        if(!hasElse) {
            Warnings.WHEN_WITHOUT_ELSE.warn(configRules, emitWarn, isFixMode,"else was not found", node.startOffset)
        }
    }

}