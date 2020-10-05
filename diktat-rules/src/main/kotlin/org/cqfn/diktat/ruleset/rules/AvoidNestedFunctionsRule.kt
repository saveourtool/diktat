package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_NESTED_FUNCTIONS
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasParent
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

class AvoidNestedFunctionsRule(private val configRules: List<RulesConfig>) : Rule("avoid-nested-functions") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FUN) {
            handleNestedFunctions(node)
            println(node.prettyPrint())
        }
    }

    private fun handleNestedFunctions(node: ASTNode) {
        if (node.hasParent(FUN)) {
            val funcName = node.getFirstChildWithType(IDENTIFIER)!!.text

            AVOID_NESTED_FUNCTIONS.warnAndFix(configRules, emitWarn, isFixMode, "fun $funcName", node.startOffset, node) {

            }
        }
    }

    private fun createCallExpression(parent: ASTNode, func: ASTNode, funcName: String) {
        val callExpr = CompositeElement(CALL_EXPRESSION)
        val refExpr = CompositeElement(REFERENCE_EXPRESSION)
        callExpr.addChild(refExpr)
        refExpr.addChild(LeafPsiElement(IDENTIFIER, funcName))
        val valueParams = func.getFirstChildWithType(VALUE_PARAMETER_LIST) !!.copyElement()
    }

    /**
     * Checks if local function has no usage of outside properties
     */
    private fun checkFunctionReferences(func: ASTNode) {
        val localProperties = mutableListOf<ASTNode>()
        func.getAllLeafsWithSpecificType(PROPERTY, localProperties)
        val propertiesNames = mutableListOf<String>()
        localProperties.forEach {
            propertiesNames.add(it.getFirstChildWithType(IDENTIFIER)!!.text)
        }
        propertiesNames.addAll(getParameterNames(func))
    }

    /**
     * Collects all function parameters' names
     * @return List of names
     */
    private fun getParameterNames(node: ASTNode): List<String> {
        val params = mutableListOf<ASTNode>()
        node.getAllLeafsWithSpecificType(VALUE_PARAMETER, params)
        val paramsNames = mutableListOf<String>()
        params.forEach {
            paramsNames.add(it.getFirstChildWithType(IDENTIFIER)!!.text)
        }
        return paramsNames
    }
}