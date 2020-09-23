package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.FILE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.utils.containsOnlyConstants
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findUsagesOf
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Variables with `val` modifier - are immutable (read-only).
 * Usage of such variables instead of `var` variables increases robustness and readability of code,
 * because `var` variables can be reassigned several times in the business logic. Of course, in some scenarios with loops or accumulators only `var`s can be used and are allowed.
 */
class ImmutableValNoVarRule(private val configRules: List<RulesConfig>) : Rule("no-var-rule") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (node.elementType == FILE) {
            println(node.prettyPrint())
        }
    }
}

