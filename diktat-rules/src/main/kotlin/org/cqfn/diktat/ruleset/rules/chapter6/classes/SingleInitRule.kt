package org.cqfn.diktat.ruleset.rules.chapter6.classes

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MULTIPLE_INIT_BLOCKS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.asAssignment
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * The rule that checks whether a class has a single `init` block or multiple. Having multiple `init` blocks is a bad practice.
 */
class SingleInitRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(MULTIPLE_INIT_BLOCKS)
) {
    override fun logic(node: ASTNode) {
        when (node.elementType) {
            CLASS_BODY -> handleInitBlocks(node)
            else -> return
        }
    }

    private fun handleInitBlocks(node: ASTNode) {
        // merge init blocks if there are multiple
        node
            .children()
            .filter { it.elementType == CLASS_INITIALIZER }
            .toList()
            .takeIf { it.size > 1 }
            ?.let { initBlocks ->
                val className = node.treeParent.getIdentifierName()?.text
                MULTIPLE_INIT_BLOCKS.warnAndFix(configRules, emitWarn, isFixMode,
                    "in class <$className> found ${initBlocks.size} `init` blocks", node.startOffset, node) {
                    mergeInitBlocks(initBlocks)
                }
            }

        // move property assignments from init block to property declarations
        node.findChildByType(CLASS_INITIALIZER)?.let { initBlock ->
            val propertiesFromPrimaryConstructor = node
                .treeParent
                .findChildByType(PRIMARY_CONSTRUCTOR)
                ?.findChildByType(VALUE_PARAMETER_LIST)
                ?.children()
                ?.filter { it.elementType == ElementType.VALUE_PARAMETER }
                ?.map { it.psi as KtParameter }
                ?.map { it.name }
                ?.toList()
            val propertiesFromClassBody = node
                .children()
                .filter { it.elementType == PROPERTY }
                .toList()
            moveAssignmentsToProperties(propertiesFromClassBody, propertiesFromPrimaryConstructor, initBlock)
        }
    }

    private fun mergeInitBlocks(initBlocks: List<ASTNode>) {
        val firstInitBlock = initBlocks.first()
        initBlocks.drop(1).forEach { initBlock ->
            firstInitBlock.findChildByType(BLOCK)?.run {
                val beforeNode = lastChildNode.treePrev.takeIf { it.elementType == WHITE_SPACE } ?: lastChildNode
                (initBlock.findChildByType(BLOCK)?.psi as? KtBlockExpression)?.statements?.forEach {
                    addChild(PsiWhiteSpaceImpl("\n"), beforeNode)
                    addChild(it.node.clone() as ASTNode, beforeNode)
                }
            }
            if (initBlock.treePrev.elementType == WHITE_SPACE && initBlock.treeNext.elementType == WHITE_SPACE) {
                initBlock.treeParent.removeChild(initBlock.treeNext)
            }
            initBlock.treeParent.removeChild(initBlock)
        }
        firstInitBlock.parent(CLASS_BODY)?.let(::removeEmptyBlocks)
    }

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun moveAssignmentsToProperties(
        propertiesFromClassBody: List<ASTNode>,
        propertiesFromPrimaryConstructor: List<String?>?,
        initBlock: ASTNode
    ) {
        initBlock
            .findChildByType(BLOCK)
            ?.run {
                (psi as KtBlockExpression)
                    .statements
                    .mapNotNull { it.asAssignment() }
                    .filter { it.left is KtNameReferenceExpression }
                    .filter { statement ->
                        statement.right?.node?.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)?.all { arg ->
                            propertiesFromClassBody.any { (it.psi as KtProperty).name == arg.text } || propertiesFromPrimaryConstructor?.any { it == arg.text } == true
                        } ?: false
                    }
                    .groupBy { assignment ->
                        val assignedRef = assignment.left as KtNameReferenceExpression
                        propertiesFromClassBody.find { (it.psi as KtProperty).name == assignedRef.getReferencedName() }
                    }
                    .filterKeys { it != null }
                    .mapKeys { (k, _) -> k as ASTNode }
                    .filter { (property, assignments) ->
                        !(property.psi as KtProperty).hasBody() && assignments.size == 1
                    }
                    .takeIf { it.isNotEmpty() }
                    ?.let { map ->
                        MULTIPLE_INIT_BLOCKS.warnAndFix(configRules, emitWarn, isFixMode,
                            "`init` block has assignments that can be moved to declarations", initBlock.startOffset, initBlock
                        ) {
                            map.forEach { (property, assignments) ->
                                val assignment = assignments.single()
                                property.addChild(PsiWhiteSpaceImpl(" "), null)
                                property.addChild(LeafPsiElement(EQ, "="), null)
                                property.addChild(PsiWhiteSpaceImpl(" "), null)
                                property.addChild(assignment.right!!.node.clone() as ASTNode, null)
                                assignment.node.run {
                                    if (treePrev.elementType == WHITE_SPACE) {
                                        treeParent.removeChild(treePrev)
                                    }
                                    treeParent.removeChild(this)
                                }
                            }
                        }
                    }
            }
        initBlock.parent(CLASS_BODY)?.let(::removeEmptyBlocks)
    }

    private fun removeEmptyBlocks(node: ASTNode) {
        node
            .getAllChildrenWithType(CLASS_INITIALIZER)
            .filter {
                (it.findChildByType(BLOCK)?.psi as KtBlockExpression?)?.statements?.isEmpty() ?: false
            }
            .forEach {
                it.treeParent.removeChild(it)
            }
    }

    companion object {
        const val NAME_ID = "multiple-init-block"
    }
}
