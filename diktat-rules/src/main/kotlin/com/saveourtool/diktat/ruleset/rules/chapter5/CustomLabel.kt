package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.CUSTOM_LABEL
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.loopType

import org.jetbrains.kotlin.KtNodeTypes.BREAK
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CONTINUE
import org.jetbrains.kotlin.KtNodeTypes.LABEL_QUALIFIER
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.RETURN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule that checks using custom label
 */
class CustomLabel(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(CUSTOM_LABEL)
) {
    private val forEachReference = listOf("forEach", "forEachIndexed")
    private val labels = listOf("@loop", "@forEach", "@forEachIndexed")
    private val stopWords = listOf(RETURN, BREAK, CONTINUE)

    override fun logic(node: ASTNode) {
        if (node.elementType == LABEL_QUALIFIER && node.text !in labels && node.treeParent.elementType in stopWords) {
            val nestedCount = node.parents().count {
                it.elementType in loopType ||
                        (it.elementType == CALL_EXPRESSION && it.findChildByType(REFERENCE_EXPRESSION)?.text in forEachReference)
            }
            if (nestedCount == 1) {
                CUSTOM_LABEL.warn(configRules, emitWarn, node.text, node.startOffset, node)
            }
        }
    }

    companion object {
        const val NAME_ID = "custom-label"
    }
}
