package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_OVERLOADING_FUNCTION_ARGUMENTS
import org.cqfn.diktat.ruleset.utils.allSiblings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class OverloadingArgumentsFunction(private val configRules: List<RulesConfig>) : Rule("overloading-default-values") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == FUN) {
            checkFun(node.psi as KtFunction)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(funPsi: KtFunction) {
        val allOverloadFunction = funPsi.node.allSiblings(withSelf = false)
                .map { it.psi }
                .filter { it.node.elementType == FUN }
                .map { it as KtFunction }
                .filter { it.nameIdentifier!!.text == funPsi.nameIdentifier!!.text && it.valueParameters.containsAll(funPsi.valueParameters) }
        if (allOverloadFunction.isNotEmpty()) {
            WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warn(configRules, emitWarn, isFixMode, funPsi.node.findChildByType(IDENTIFIER)!!.text, funPsi.startOffset, funPsi.node)
        }
    }
}
