package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_TYPE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.LAMBDA_IS_NOT_LAST_PARAMETER
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

class LambdaParameterOrder(private val configRules: List<RulesConfig>) : Rule("lambda-parameter-order") {


    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.FUN) {
            checkArguments(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkArguments(node: ASTNode) {
        val funArguments = (node.psi as KtFunction).valueParameters
        val sortArguments = funArguments.sortedBy { it.typeReference?.node?.hasChildOfType(FUNCTION_TYPE) }
        funArguments.filterIndexed {index, ktParameter -> ktParameter != sortArguments[index] }.ifNotEmpty {
            LAMBDA_IS_NOT_LAST_PARAMETER.warn(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)!!.text,
                    first().node.startOffset, node)
        }
    }
}
