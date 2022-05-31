package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_NESTED_FUNCTIONS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.hasParent

import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks for nested functions and warns if it finds any.
 */
class AvoidNestedFunctionsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(AVOID_NESTED_FUNCTIONS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == FUN) {
            handleNestedFunctions(node)
        }
    }

    // FixMe: need to detect all properties, which local function is using and add them to params of this function
    @Suppress("UnsafeCallOnNullableType")
    private fun handleNestedFunctions(node: ASTNode) {
        if (isNestedFunction(node)) {
            val funcName = node.getFirstChildWithType(IDENTIFIER)!!.text

            AVOID_NESTED_FUNCTIONS.warnAndFix(configRules, emitWarn, isFixMode, "fun $funcName", node.startOffset, node,
                canBeAutoCorrected = checkFunctionReferences(node)) {
                // We take last nested function, then add and remove child from bottom to top
                val lastFunc = node.findAllDescendantsWithSpecificType(FUN).last()
                val funcSeq = lastFunc
                    .parents()
                    .filter { it.elementType == FUN }
                    .toMutableList()
                funcSeq.add(0, lastFunc)
                val firstFunc = funcSeq.last()
                funcSeq.dropLast(1).forEachIndexed { index, it ->
                    val parent = funcSeq[index + 1]
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

    private fun isNestedFunction(node: ASTNode): Boolean =
            node.hasParent(FUN) && node.hasFunParentUntil(CLASS_BODY) && !node.hasChildOfType(MODIFIER_LIST)
                    && node.getFirstChildWithType(IDENTIFIER) != null

    private fun ASTNode.hasFunParentUntil(stopNode: IElementType): Boolean =
            parents()
                .asSequence()
                .takeWhile { it.elementType != stopNode }
                .any { it.elementType == FUN }

    /**
     * Checks if local function has no usage of outside properties
     */
    @Suppress("UnsafeCallOnNullableType", "FUNCTION_BOOLEAN_PREFIX")
    private fun checkFunctionReferences(func: ASTNode): Boolean {
        val localProperties: MutableList<ASTNode> = mutableListOf()
        localProperties.addAll(func.findAllDescendantsWithSpecificType(PROPERTY))
        val propertiesNames: List<String> = mutableListOf<String>().apply {
            addAll(localProperties.map { it.getFirstChildWithType(IDENTIFIER)!!.text })
            addAll(getParameterNames(func))
        }
            .toList()

        return func.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION).all { propertiesNames.contains(it.text) }
    }

    /**
     * Collects all function parameters' names
     *
     * @return List of names
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun getParameterNames(node: ASTNode): List<String> =
            (node.psi as KtFunction).valueParameters.map { it.name!! }

    companion object {
        const val NAME_ID = "acj-avoid-nested-functions"
    }
}
