package com.saveourtool.diktat.ruleset.rules.chapter4

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.SMART_CAST_NEEDED
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.findParentNodeWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.hasParent
import com.saveourtool.diktat.ruleset.utils.search.findAllVariablesWithUsages

import org.jetbrains.kotlin.KtNodeTypes.BINARY_WITH_TYPE
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.ELSE
import org.jetbrains.kotlin.KtNodeTypes.FLOAT_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.IF
import org.jetbrains.kotlin.KtNodeTypes.INTEGER_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.IS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.STRING_TEMPLATE
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.KtNodeTypes.WHEN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * Rule that detects redundant explicit casts
 */
class SmartCastRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(SMART_CAST_NEEDED)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            val usages = collectLocalPropertiesWithUsages(node)
            val properMap = collectReferenceList(usages)
            handleProp(properMap)
        }

        if (node.elementType == WHEN) {
            // Rule is simplified after https://github.com/saveourtool/diktat/issues/1168
            return
        }
    }

    // Divide in is and as expr
    @Suppress("TYPE_ALIAS")
    private fun handleProp(propMap: Map<KtProperty, List<KtNameReferenceExpression>>) {
        propMap.forEach { (property, references) ->
            val isExpr: MutableList<KtNameReferenceExpression> = mutableListOf()
            val asExpr: MutableList<KtNameReferenceExpression> = mutableListOf()
            references.forEach {
                if (it.node.hasParent(IS_EXPRESSION)) {
                    isExpr.add(it)
                } else if (it.node.hasParent(BINARY_WITH_TYPE) && it.node.treeParent.text.contains(KtTokens.AS_KEYWORD.value)) {
                    asExpr.add(it)
                }
            }
            val groups = groupIsAndAsExpr(isExpr, asExpr, property)
            if (groups.isNotEmpty()) {
                handleGroups(groups)
            }
        }
    }

    /**
     * If condition == is then we are looking for then block
     * If condition == !is then we are looking for else block
     */
    @Suppress("NestedBlockDepth", "TYPE_ALIAS")
    private fun handleGroups(groups: Map<KtNameReferenceExpression, List<KtNameReferenceExpression>>) {
        groups.keys.forEach { key ->
            val parentText = key.node.treeParent.text
            if (parentText.contains(" is ")) {
                groups.getValue(key).forEach { asCall ->
                    if (asCall.node.hasParent(THEN)) {
                        raiseWarning(asCall.node)
                    }
                }
            } else if (parentText.contains(" !is ")) {
                groups.getValue(key).forEach { asCall ->
                    if (asCall.node.hasParent(ELSE)) {
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
            val text = afterDotPart?.let { "${asCall.text}.$afterDotPart" } ?: asCall.text
            (dotExpr ?: asCall.treeParent).treeParent.addChild(KotlinParser().createNode(text), (dotExpr ?: asCall.treeParent))
            (dotExpr ?: asCall.treeParent).treeParent.removeChild((dotExpr ?: asCall.treeParent))
        }
    }

    /**
     * Groups is and as expressions, so that they are used in same if block
     */
    @Suppress("TYPE_ALIAS")
    private fun groupIsAndAsExpr(isExpr: List<KtNameReferenceExpression>,
                                 asExpr: List<KtNameReferenceExpression>,
                                 prop: KtProperty
    ): Map<KtNameReferenceExpression, List<KtNameReferenceExpression>> {
        if (isExpr.isEmpty() && asExpr.isNotEmpty()) {
            handleZeroIsCase(asExpr, prop)
            return emptyMap()
        }

        val groupedExprs: MutableMap<KtNameReferenceExpression, List<KtNameReferenceExpression>> = mutableMapOf()
        isExpr.forEach { ref ->
            val list: MutableList<KtNameReferenceExpression> = mutableListOf()
            asExpr.forEach { asCall ->
                if (asCall.node.findParentNodeWithSpecificType(IF)
                        == ref.node.findParentNodeWithSpecificType(IF)) {
                    list.add(asCall)
                }
            }
            groupedExprs[ref] = list
        }
        return groupedExprs
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getPropertyType(prop: KtProperty): String? {
        when (prop.initializer?.node?.elementType) {
            STRING_TEMPLATE -> return "String"
            INTEGER_CONSTANT -> return "Int"
            else -> {
            }
        }
        if (prop.initializer?.node?.elementType == FLOAT_CONSTANT) {
            if (prop.initializer?.text!!.endsWith("f")) {
                return "Float"
            }
            return "Double"
        }
        return null
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleZeroIsCase(asExpr: List<KtNameReferenceExpression>, prop: KtProperty) {
        val propType = getPropertyType(prop) ?: return
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

        thenBlock?.let {
            // Find all as expressions that are inside this current block
            val asList = thenBlock
                .findAllDescendantsWithSpecificType(BINARY_WITH_TYPE)
                .filter {
                    it.text.contains(" as ") && it.findParentNodeWithSpecificType(BLOCK) == thenBlock
                }
                .filterNot { (it.getFirstChildWithType(REFERENCE_EXPRESSION)?.psi as KtNameReferenceExpression).getLocalDeclaration() != null }
            checkAsExpressions(asList, blocks)
        }
            ?: run {
                val asList = then.findAllDescendantsWithSpecificType(BINARY_WITH_TYPE).filter { it.text.contains(KtTokens.AS_KEYWORD.value) }
                checkAsExpressions(asList, blocks)
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkAsExpressions(asList: List<ASTNode>, blocks: List<IsExpressions>) {
        val asExpr: MutableList<AsExpressions> = mutableListOf()

        @Suppress("PARAMETER_NAME_IN_OUTER_LAMBDA")
        asList.forEach {
            val split = it.text.split("as").map { part -> part.trim() }
            asExpr.add(AsExpressions(split[0], split[1], it))
        }

        val exprToChange = asExpr.filter { asCall ->
            blocks.any { isExpr ->
                isExpr.identifier == asCall.identifier &&
                        isExpr.type == asCall.type
            }
        }

        if (exprToChange.isNotEmpty()) {
            exprToChange.forEach { asCall ->
                SMART_CAST_NEEDED.warnAndFix(configRules, emitWarn, isFixMode, "${asCall.identifier} as ${asCall.type}", asCall.node.startOffset,
                    asCall.node) {
                    val dotExpr = asCall.node.findParentNodeWithSpecificType(DOT_QUALIFIED_EXPRESSION)!!
                    val afterDotPart = dotExpr.text.split(".")[1]
                    val text = "${asCall.identifier}.$afterDotPart"
                    dotExpr.treeParent.addChild(KotlinParser().createNode(text), dotExpr)
                    dotExpr.treeParent.removeChild(dotExpr)
                }
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
     *
     * @return Map of property and list of expressions
     */
    @Suppress("TYPE_ALIAS")
    private fun collectReferenceList(propertiesToUsages: Map<KtProperty, List<KtNameReferenceExpression>>): Map<KtProperty, List<KtNameReferenceExpression>> =
        propertiesToUsages.mapValues { (_, value) ->
            value.filter { entry ->
                entry.parent.node.elementType == IS_EXPRESSION || entry.parent.node.elementType == BINARY_WITH_TYPE
            }
        }

    /**
     * @property identifier a reference that is cast
     * @property type a type to which the reference is being cast
     * @property node a node that holds the entire expression
     */
    data class AsExpressions(
        val identifier: String,
        val type: String,
        val node: ASTNode
    )

    /**
     * @property identifier a reference that is checked
     * @property type a type with which the reference is being compared
     */
    data class IsExpressions(val identifier: String, val type: String)

    companion object {
        const val NAME_ID = "smart-cast-rule"
    }
}
