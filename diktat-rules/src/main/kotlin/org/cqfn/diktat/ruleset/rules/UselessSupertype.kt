package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OPEN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SUPER_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_CALL_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.USELESS_SUPERTYPE
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * rule 6.1.5
 * Explicit supertype qualification should not be used if there is not clash between called methods
 * fixme can't fix supertypes that are defined in other files.
 */
class UselessSupertype(private val configRules: List<RulesConfig>) : Rule("useless-override") {

    companion object {
        private val SUPER_TYPE = listOf(SUPER_TYPE_CALL_ENTRY, SUPER_TYPE_ENTRY)
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS)
            checkClass(node)
    }

    private fun checkClass(node: ASTNode) {
        val superNodes = node.findChildByType(SUPER_TYPE_LIST)?.findAllNodesWithCondition({ it.elementType in SUPER_TYPE })?.takeIf { it.isNotEmpty() } ?: return
        val qualifiedSuperCalls = node.findAllNodesWithSpecificType(DOT_QUALIFIED_EXPRESSION)
                .mapNotNull { findFunWithSuper(it) }.ifEmpty { return }
        if (superNodes.size == 1) {
            qualifiedSuperCalls.map { removeSupertype(it.first) }
        } else {
            handleManyImpl(superNodes, qualifiedSuperCalls)
        }
    }

    private fun handleManyImpl(superNodes: List<ASTNode>, overrideNodes: List<Pair<ASTNode, ASTNode>>) {
        val uselessSuperType = findAllSupers(superNodes, overrideNodes.map { it.second.text })?.filter { it.value == 1 }?.map { it.key }
                ?: return
        overrideNodes
                .filter {
                    it.second.text in uselessSuperType
                }.map {
                    removeSupertype(it.first)
                }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun removeSupertype(node: ASTNode) {
        USELESS_SUPERTYPE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
            val startNode = node.parent({ it.elementType == SUPER_EXPRESSION })!!.findChildByType(REFERENCE_EXPRESSION)!!
            val lastNode = startNode.siblings(true).last()
            startNode.treeParent.removeRange(startNode.treeNext, lastNode)
            startNode.treeParent.removeChild(lastNode)
        }
    }

    /**
     * Method finds pair of identifier supertype and method name else return null
     * example: super<A>.foo() -> return Pair(A, foo)
     *          super.foo() -> null
     * @param node - node of type DOT_QUALIFIED_EXPRESSION
     *
     * @return pair of identifier
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun findFunWithSuper(node: ASTNode): Pair<ASTNode, ASTNode>? {
        return Pair(
                node.findChildByType(SUPER_EXPRESSION)
                        ?.findChildByType(TYPE_REFERENCE)
                        ?.findAllNodesWithSpecificType(IDENTIFIER)
                        ?.firstOrNull() ?: return null,
                node.findChildByType(CALL_EXPRESSION)
                        ?.findAllNodesWithSpecificType(IDENTIFIER)
                        ?.firstOrNull() ?: return null)
    }

    /**
     * The method looks in the same file for all super interfaces or a class, in each it looks for methods
     * that can be overridden and creates a map with a key - the name of the method and value - the number of times it meets
     *
     * @param superTypeList - list of identifiers super classes
     * @param methodsName - name of overrides methods
     *
     * @return map name of method and the number of times it meets
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun findAllSupers(superTypeList: List<ASTNode>, methodsName: List<String>): Map<String, Int>? {
        val fileNode = superTypeList.first().parent({ it.elementType == FILE })!!
        val superNodesIdentifier = superTypeList.map { it.findAllNodesWithSpecificType(IDENTIFIER).first().text }
        val superNodes = fileNode.findAllNodesWithCondition({ superClass ->
            superClass.elementType == CLASS &&
                    superClass.getIdentifierName()!!.text in superNodesIdentifier
        }).mapNotNull { it.findChildByType(CLASS_BODY) }
        if (superNodes.size != superTypeList.size)
            return null
        val functionNameMap = mutableMapOf<String, Int>()
        superNodes.forEach { classBody ->
            val overrideFunctions = classBody.findAllNodesWithSpecificType(FUN)
                    .filter {
                        (if (classBody.treeParent.hasChildOfType(CLASS_KEYWORD)) it.findChildByType(MODIFIER_LIST)!!.hasChildOfType(OPEN_KEYWORD) else true) &&
                                it.getIdentifierName()!!.text in methodsName
                    }
            overrideFunctions.forEach {
                functionNameMap[it.getIdentifierName()!!.text] = functionNameMap.getOrDefault(it.getIdentifierName()!!.text, 0) + 1
                /*if (functionNameMap.containsKey(it.getIdentifierName()!!.text))
                    functionNameMap[it.getIdentifierName()!!.text] = functionNameMap[it.getIdentifierName()!!.text]!! + 1
                else
                    functionNameMap[it.getIdentifierName()!!.text] = 1*/
            }
        }
        return functionNameMap.toMap()
    }
}
