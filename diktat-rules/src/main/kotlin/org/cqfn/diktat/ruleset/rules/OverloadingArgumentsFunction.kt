package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_OVERLOADING_FUNCTION_ARGUMENTS
import org.cqfn.diktat.ruleset.utils.allSiblings
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class OverloadingArgumentsFunction(private val configRules: List<RulesConfig>) : Rule("overloading-default-values") {

    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == FUN) {
            checkFun(node.psi as KtFunction)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(funPsi: KtFunction) {
        val allOverloadFunction = funPsi.node.allSiblings(withSelf = false)
                .asSequence()
                .filter { it.elementType == FUN }
                .map { it.psi as KtFunction }
                .filter { it.nameIdentifier!!.text == funPsi.nameIdentifier!!.text && it.valueParameters.containsAll(funPsi.valueParameters) }
                .filter { funPsi.node.findChildBefore(IDENTIFIER, TYPE_REFERENCE)?.text == it.node.findChildBefore(IDENTIFIER, TYPE_REFERENCE)?.text }
                .filter  {funPsi.node.findChildAfter(IDENTIFIER, TYPE_REFERENCE)?.text == it.node.findChildAfter(IDENTIFIER, TYPE_REFERENCE)?.text }
                .toList()
        if (allOverloadFunction.isNotEmpty()) {
            WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warn(configRules, emitWarn, isFixMode, funPsi.node.findChildByType(IDENTIFIER)!!.text, funPsi.startOffset, funPsi.node)
        }
    }
}
