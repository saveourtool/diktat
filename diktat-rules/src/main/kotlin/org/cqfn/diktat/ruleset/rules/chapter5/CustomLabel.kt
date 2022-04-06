package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.CUSTOM_LABEL
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.loopType

import com.pinterest.ktlint.core.ast.ElementType.BREAK
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CONTINUE
import com.pinterest.ktlint.core.ast.ElementType.LABEL_QUALIFIER
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RETURN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule that checks using custom label
 */
class CustomLabel(configRules: List<RulesConfig>) : DiktatRule(
    "abk-custom-label",
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
                CUSTOM_LABEL.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
            }
        }
    }

    companion object{
        val nameId = "abk-custom-label"
    }
}
