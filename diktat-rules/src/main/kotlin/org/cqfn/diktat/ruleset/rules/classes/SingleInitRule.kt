package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.jetbrains.kotlin.backend.common.onlyIf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.psiUtil.children

class SingleInitRule(private val configRule: List<RulesConfig>) : Rule("multiple-init-block") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            CLASS_BODY -> handleInitBlocks(node)
        }
    }

    private fun handleInitBlocks(node: ASTNode) =
        node.children()
            .filter { it.elementType == CLASS_INITIALIZER }
            .toList()
            .onlyIf({ size > 1 }) { initBlocks ->
                val className = node.treeParent.getIdentifierName()?.text
                Warnings.MULTIPLE_INIT_BLOCKS.warnAndFix(configRule, emitWarn, isFixMode,
                    "in class <$className> found ${initBlocks.size} `init` blocks", node.startOffset, node) {
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
                }
            }

}
