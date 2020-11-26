package org.cqfn.diktat.ruleset.rules.classes

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.asAssignment
import org.jetbrains.kotlin.psi.psiUtil.children

class SingleInitRule(private val configRule: List<RulesConfig>) : Rule("multiple-init-block") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

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
                Warnings.MULTIPLE_INIT_BLOCKS.warnAndFix(configRule, emitWarn, isFixMode,
                    "in class <$className> found ${initBlocks.size} `init` blocks", node.startOffset, node) {
                    mergeInitBlocks(initBlocks)
                }
            }

        // move property assignments from init block to property declarations
        node.findChildByType(CLASS_INITIALIZER)?.let { initBlock ->
            val properties = node
                .children()
                .filter { it.elementType == PROPERTY }
                .toList()
            moveAssignmentsToProperties(properties, initBlock)
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

    @Suppress("UnsafeCallOnNullableType")
    private fun moveAssignmentsToProperties(properties: List<ASTNode>, initBlock: ASTNode) {
        initBlock
            .findChildByType(BLOCK)
            ?.run {
                (psi as KtBlockExpression)
                    .statements
                    .mapNotNull { it.asAssignment() }
                    .filter { it.left is KtNameReferenceExpression }
                    .groupBy { assignment ->
                        val assignedRef = assignment.left as KtNameReferenceExpression
                        properties.find { (it.psi as KtProperty).name == assignedRef.getReferencedName() }
                    }
                    .filterKeys { it != null }
                    .mapKeys { (k, _) -> k as ASTNode }
                    .filter { (property, assignments) ->
                        !(property.psi as KtProperty).hasBody() && assignments.size == 1
                    }
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        Warnings.MULTIPLE_INIT_BLOCKS.warnAndFix(configRule, emitWarn, isFixMode,
                            "`init` block has assignments that can be moved to declarations", initBlock.startOffset, initBlock
                        ) {
                            it.forEach { (property, assignments) ->
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
}
