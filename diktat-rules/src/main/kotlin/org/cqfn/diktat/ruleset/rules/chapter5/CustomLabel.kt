package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.CUSTOM_LABEL
import org.cqfn.diktat.ruleset.utils.loopType

import com.pinterest.ktlint.core.Rule
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
class CustomLabel(private val configRules: List<RulesConfig>) : Rule("custom-label") {
    private var isFixMode: Boolean = false
    private val forEachReference = listOf("forEach", "forEachIndexed")
    private val labels = listOf("@loop", "@forEach", "@forEachIndexed")
    private val stopWords = listOf(RETURN, BREAK, CONTINUE)
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

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
}
