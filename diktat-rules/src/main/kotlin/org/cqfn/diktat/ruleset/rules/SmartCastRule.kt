package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_WITH_TYPE
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.IS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_IS_PATTERN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.SMART_CAST_NEEDED
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findParentNodeWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.hasParent
import org.cqfn.diktat.ruleset.utils.search.findAllVariablesWithUsages
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.hasActualModifier
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents

class SmartCastRule(private val configRules: List<RulesConfig>) : Rule("smart-cast-rule") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FILE) {
            val usages = collectLocalPropertiesWithUsages(node)
            val properMap = collectReferenceList(usages)
            handleProp(properMap)
        }

        if (node.elementType == WHEN) {
            handleWhenCondition(node)
        }
    }

    // Divide in is and as expr
    private fun handleProp(propMap: Map<KtProperty, List<KtNameReferenceExpression>>) {
        propMap.forEach { (property, references) ->
            val isExpr = mutableListOf<KtNameReferenceExpression>()
            val asExpr = mutableListOf<KtNameReferenceExpression>()
            references.forEach {
                if (it.node.hasParent(IS_EXPRESSION))
                    isExpr.add(it)
                else if (it.node.hasParent(BINARY_WITH_TYPE)
                        && it.node.treeParent.text.contains(KtTokens.AS_KEYWORD.value))
                    asExpr.add(it)
            }
            val groups = groupIsAndAsExpr(isExpr, asExpr, property)
            if (groups.isNotEmpty())
                handleGroups(groups)
        }
    }

    /**
     * If condition == is then we are looking for then block
     * If condition == !is then we are looking for else block
     */
    @Suppress("NestedBlockDepth")
    private fun handleGroups(groups: Map<KtNameReferenceExpression, List<KtNameReferenceExpression>>) {
        groups.keys.forEach {
            if (it.node.treeParent.text.contains(" is ")) {
                groups.getValue(it).forEach { asCall ->
                    if (asCall.node.findParentNodeWithSpecificType(THEN) != null) {
                        raiseWarning(asCall.node)
                    }
                }
            } else if (it.node.treeParent.text.contains(" !is ")) {
                groups.getValue(it).forEach { asCall ->
                    if (asCall.node.findParentNodeWithSpecificType(ELSE) != null) {
                        raiseWarning(asCall.node)
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun raiseWarning(asCall: ASTNode) {
        SMART_CAST_NEEDED.warnAndFix(configRules, emitWarn, isFixMode, asCall.treeParent.text, asCall.startOffset,
                asCall) {
            val dotExpr = asCall.findParentNodeWithSpecificType(DOT_QUALIFIED_EXPRESSION)
            val afterDotPart = dotExpr?.text?.split(".")?.get(1)
            val text = if (afterDotPart != null) "${asCall.text}.$afterDotPart" else asCall.text
            (dotExpr ?: asCall.treeParent).treeParent.addChild(KotlinParser().createNode(text), (dotExpr ?: asCall.treeParent))
            (dotExpr ?: asCall.treeParent).treeParent.removeChild((dotExpr ?: asCall.treeParent))
        }
    }

    /**
     * Groups is and as expressions, so that they are used in same if block
     */
    private fun groupIsAndAsExpr(isExpr: List<KtNameReferenceExpression>,
                                 asExpr: List<KtNameReferenceExpression>,
                                 prop: KtProperty)
            : Map<KtNameReferenceExpression, List<KtNameReferenceExpression>> {
        val groupedExprs = mutableMapOf<KtNameReferenceExpression, List<KtNameReferenceExpression>>()

        if (isExpr.isEmpty() && asExpr.isNotEmpty()) {
            handleZeroIsCase(asExpr, prop)
            return emptyMap()
        }

        isExpr.forEach {
            val list = mutableListOf<KtNameReferenceExpression>()
            asExpr.forEach { asCall ->
                if (asCall.node.findParentNodeWithSpecificType(IF)
                        == it.node.findParentNodeWithSpecificType(IF))
                    list.add(asCall)
            }
            groupedExprs[it] = list
        }
        return groupedExprs
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getPropertyType(prop: KtProperty): String? {
        when (prop.initializer?.node?.elementType) {
            STRING_TEMPLATE -> return "String"
            INTEGER_CONSTANT -> return "Int"
        }
        if (prop.initializer?.node?.elementType == FLOAT_CONSTANT) {
            if (prop.initializer?.text!!.endsWith("f"))
                return "Float"
            return "Double"
        }
        return null
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleZeroIsCase(asExpr: List<KtNameReferenceExpression>, prop: KtProperty) {
        val propType = getPropertyType(prop)
        if (propType.isNullOrBlank())
            return
        asExpr
                .map { it.node }
                .forEach {
                    if (it.treeParent.text.endsWith(propType)) {
                        raiseWarning(it)
                    }
                }
    }


    private fun handleThenBlock(then: ASTNode, blocks: List<IsExpressions>) {
        val thenBlock = then.findChildByType(BLOCK)

        if (thenBlock != null) {
            // Find all as expressions that are inside this current block
            val asList = thenBlock.findAllNodesWithSpecificType(BINARY_WITH_TYPE).filter {
                it.text.contains(" as ")
                        && it.findParentNodeWithSpecificType(BLOCK) == thenBlock
            }
                    .filterNot { (it.getFirstChildWithType(REFERENCE_EXPRESSION)?.psi as KtNameReferenceExpression).getLocalDeclaration() != null }
            checkAsExpressions(asList, blocks)
        } else {
            val asList = then.findAllNodesWithSpecificType(BINARY_WITH_TYPE).filter { it.text.contains(KtTokens.AS_KEYWORD.value) }
            checkAsExpressions(asList, blocks)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkAsExpressions(asList: List<ASTNode>, blocks: List<IsExpressions>) {
        val asExpr = mutableListOf<AsExpressions>()

        asList.forEach {
            val split = it.text.split("as").map { part -> part.trim() }
            asExpr.add(AsExpressions(split[0], split[1], it))
        }

        val exprToChange = asExpr.filter {
            blocks.any { isExpr ->
                isExpr.identifier == it.identifier
                        && isExpr.type == it.type
            }
        }

        if (exprToChange.isNotEmpty()) {
            exprToChange.forEach {
                SMART_CAST_NEEDED.warnAndFix(configRules, emitWarn, isFixMode, "${it.identifier} as ${it.type}", it.node.startOffset,
                        it.node) {
                    val dotExpr = it.node.findParentNodeWithSpecificType(DOT_QUALIFIED_EXPRESSION)!!
                    val afterDotPart = dotExpr.text.split(".")[1]
                    val text = "${it.identifier}.$afterDotPart"
                    dotExpr.treeParent.addChild(KotlinParser().createNode(text), dotExpr)
                    dotExpr.treeParent.removeChild(dotExpr)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleWhenCondition(node: ASTNode) {
        /*
            Check if there is WHEN_CONDITION_IS_PATTERN. If so delete 'as' in it's block
            or call expression if it doesn't have block
         */

        val identifier = node.getFirstChildWithType(REFERENCE_EXPRESSION)?.text

        node.getAllChildrenWithType(WHEN_ENTRY).forEach {
            if (it.hasChildOfType(WHEN_CONDITION_IS_PATTERN) && identifier != null) {
                val type = it.getFirstChildWithType(WHEN_CONDITION_IS_PATTERN)!!
                        .getFirstChildWithType(TYPE_REFERENCE)!!.text

                val callExpr = it.findAllNodesWithSpecificType(BINARY_WITH_TYPE).firstOrNull()
                val blocks = listOf(IsExpressions(identifier, type))

                if (callExpr != null)
                    handleThenBlock(callExpr, blocks)
            }
        }
    }

    private fun KtNameReferenceExpression.getLocalDeclaration(): KtProperty? = parents
            .mapNotNull { it as? KtBlockExpression }
            .first()
            .let { blockExpression ->
                blockExpression
                        .statements
                        .takeWhile { !it.isAncestor(this, true) }
                        .mapNotNull { it as? KtProperty }
                        .find {
                            it.isLocal &&
                                    it.hasInitializer() &&
                                    it.name?.equals(getReferencedName())
                                    ?: false
                        }
            }

    private fun collectLocalPropertiesWithUsages(node: ASTNode) = node
            .findAllVariablesWithUsages { propertyNode ->
                propertyNode.name != null
            }

    /**
     * Gets references, which contains is or as keywords
     * @return Map of property and list of expressions
     */
    private fun collectReferenceList(propertiesToUsages: Map<KtProperty, List<KtNameReferenceExpression>>)
            : Map<KtProperty, List<KtNameReferenceExpression>> =
            propertiesToUsages.mapValues { (_, value) ->
                value.filter { entry ->
                    entry.parent.node.elementType == IS_EXPRESSION
                            || entry.parent.node.elementType == BINARY_WITH_TYPE
                }
            }


    class AsExpressions(val identifier: String, val type: String, val node: ASTNode)

    class IsExpressions(val identifier: String, val type: String)
}
