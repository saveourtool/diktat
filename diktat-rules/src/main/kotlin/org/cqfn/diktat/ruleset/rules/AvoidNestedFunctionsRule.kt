package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_NESTED_FUNCTIONS
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findParentNodeWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasParent
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks for nested functions and warns if it finds any.
 */
class AvoidNestedFunctionsRule(private val configRules: List<RulesConfig>) : Rule("avoid-nested-functions") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FUN) {
            handleNestedFunctions(node)
        }
    }

    // FixMe: need to detect all properties, which local function is using and add them to params of this function
    @Suppress("UnsafeCallOnNullableType")
    private fun handleNestedFunctions(node: ASTNode) {
        if (node.hasParent(FUN)) {
            val funcName = node.getFirstChildWithType(IDENTIFIER)!!.text

            AVOID_NESTED_FUNCTIONS.warnAndFix(configRules, emitWarn, isFixMode, "fun $funcName", node.startOffset, node,
                    canBeAutoCorrected = checkFunctionReferences(node)) {
                // We take last nested function, then add and remove child from bottom to top
                val lastFunc = node.findAllNodesWithSpecificType(FUN).last()
                val funcSeq = lastFunc.parents().filter { it.elementType == FUN }.toMutableList()
                funcSeq.add(0, lastFunc)
                val firstFunc = funcSeq.last()

                funcSeq.forEach {
                    if (it != firstFunc) {
                        val parent = it.findParentNodeWithSpecificType(FUN)!!
                        if (it.treePrev.isWhiteSpaceWithNewline()) {
                            parent.removeChild(it.treePrev)
                        }
                        firstFunc.treeParent.addChild(it.clone() as ASTNode, firstFunc)
                        firstFunc.treeParent.addChild(PsiWhiteSpaceImpl("\n"), firstFunc)
                        parent.removeChild(it)
                    }
                }
            }
        }
    }

    /**
     * Checks if local function has no usage of outside properties
     */
    private fun checkFunctionReferences(func: ASTNode): Boolean {
        val localProperties = mutableListOf<ASTNode>()
        func.getAllLeafsWithSpecificType(PROPERTY, localProperties)
        val propertiesNames = mutableListOf<String>()
        localProperties.forEach {
            propertiesNames.add(it.getFirstChildWithType(IDENTIFIER)!!.text)
        }
        propertiesNames.addAll(getParameterNames(func))

        return func.findAllNodesWithSpecificType(REFERENCE_EXPRESSION).all { propertiesNames.contains(it.text) }
    }

    /**
     * Collects all function parameters' names
     * @return List of names
     */
    private fun getParameterNames(node: ASTNode): List<String> {
        val paramsNames = mutableListOf<String>()
        (node.psi as KtFunction).valueParameters.forEach {
            paramsNames.add(it.name ?: "_")
        }
        return paramsNames
    }
}
