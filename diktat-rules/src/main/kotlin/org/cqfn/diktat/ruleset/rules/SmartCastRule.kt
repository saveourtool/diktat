package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BINARY_WITH_TYPE
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_IS_PATTERN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.SMART_CAST_NEEDED
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.*
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

        if (node.elementType == IF) {
            handleIfBlock(node)
        }

        if (node.elementType == WHEN) {
            handleWhenCondition(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleIfBlock(node: ASTNode) {
        /*
            There are 2 conditions.
            1) if has 'is' in condition, then we check all 'as' keywords in 'then block'
            2) if has '!is' -> NOT_IS in condition, then we check all 'as' keywords in else's 'then block'
         */
        val isBlocks = mutableListOf<IsExpressions>()
        val notIsBlocks = mutableListOf<IsExpressions>()

        val conditionBlock = node.getFirstChildWithType(CONDITION)!!

        conditionBlock.findAllNodesWithSpecificType(IS_EXPRESSION).map { it.text }.forEach {
            if (it.contains("!is")) {
                val split = it.split("!is").map { part -> part.trim() }
                notIsBlocks.add(IsExpressions(split[0], split[1]))
            } else {
                val split = it.split("is").map { part -> part.trim() }
                isBlocks.add(IsExpressions(split[0], split[1]))
            }
        }

        val elseBlock = node.findAllNodesWithSpecificType(ELSE).firstOrNull { it.firstChildNode.elementType != IF }

        if (isBlocks.isNotEmpty()) {
            val then = node.findChildByType(THEN)!!
            handleThenBlock(then, isBlocks)
        }

        if (notIsBlocks.isNotEmpty() && elseBlock != null) {
            handleThenBlock(elseBlock, notIsBlocks)
        }
    }

    private fun handleThenBlock(then: ASTNode, blocks: List<IsExpressions>) {
        val thenBlock = then.findChildByType(BLOCK)

        if (thenBlock != null) {
            // Find all as expressions that are inside this current block
            val asList = thenBlock.findAllNodesWithSpecificType(BINARY_WITH_TYPE).filter { it.text.contains(" as ")
                    && it.findParentNodeWithSpecificType(BLOCK) == thenBlock }
                    .filterNot { (it.getFirstChildWithType(REFERENCE_EXPRESSION)?.psi as KtNameReferenceExpression).hasLocalDeclaration() != null }
            checkAsExpressions(asList, blocks)
        } else {
            val asList = then.findAllNodesWithSpecificType(BINARY_WITH_TYPE).filter {  it.text.contains(" as ") }
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

                val callExpr = it.getFirstChildWithType(CALL_EXPRESSION)!!
                val blocks = listOf(IsExpressions(identifier, type))

                handleThenBlock(callExpr, blocks)
            }
        }
    }

    private fun KtNameReferenceExpression.hasLocalDeclaration(): KtProperty? = parents
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

    class AsExpressions(val identifier: String, val type: String, val node:ASTNode)

    class IsExpressions(val identifier: String, val type: String)
}
