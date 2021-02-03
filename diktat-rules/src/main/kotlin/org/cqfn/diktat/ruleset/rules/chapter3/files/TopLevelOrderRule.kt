package org.cqfn.diktat.ruleset.rules.chapter3.files

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.TOP_LEVEL_ORDER
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Rule that checks order in top level
 */
class TopLevelOrderRule(private val configRules: List<RulesConfig>) : Rule("top-level-order") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FILE) {
            checkNode(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkNode(node: ASTNode) {
        val children = node.getChildren(null)
        val ownOrder = children.filter { it.elementType in sortedType }
        if (ownOrder.isEmpty()) {
            return
        }
        val properties = Properties(children.filter { it.elementType == PROPERTY }).sortElements()
        val functions = children.filter { it.elementType == FUN }
        val classes = children.filter { it.elementType == CLASS }
        val sortOrder = Blocks(properties, functions, classes).sortElements().map { astNode ->
            Pair(astNode, astNode.siblings(false).takeWhile { it.elementType == WHITE_SPACE || it.isPartOfComment() }.toList())
        }
        val lastNonSortedChildren = ownOrder.last().siblings(true).toList()
        sortOrder.filterIndexed { index, pair -> ownOrder[index] != pair.first }
            .forEach { listOfChildren ->
                val wrongNode = listOfChildren.first
                TOP_LEVEL_ORDER.warnAndFix(configRules, emitWarn, isFixMode, wrongNode.text, wrongNode.startOffset, wrongNode) {
                    node.removeRange(node.findChildByType(IMPORT_LIST)!!.treeNext, node.lastChildNode)
                    node.removeChild(node.lastChildNode)
                    sortOrder.map { bodyChild ->
                        bodyChild.second.reversed().map { node.addChild(it, null) }
                        node.addChild(bodyChild.first, null)
                    }
                    lastNonSortedChildren.map { node.addChild(it, null) }
                }
            }
    }

    /**
     * Interface for classes to collect child and sort them
     */
    interface Elements {
        /**
         * Method to sort children
         *
         * @return sorted mutable list
         */
        fun sortElements(): MutableList<ASTNode>
    }

    /**
     * Class containing different groups of properties in file
     */
    private data class Properties(private val properties: List<ASTNode>) : Elements {
        override fun sortElements(): MutableList<ASTNode> {
            val constValProperties = properties.filter { it.isConstant() }
            val valProperties = properties.filter { it.isValProperty() && !it.isConstant() }
            val lateinitProperties = properties.filter { it.isLateInit() }
            val varProperties = properties.filter { it.isVarProperty() && !it.isLateInit() }
            return listOf(constValProperties, valProperties, lateinitProperties, varProperties).flatten().toMutableList()
        }
    }

    /**
     * Class containing different children in file
     */
    private data class Blocks(
        private val properties: List<ASTNode>,
        private val functions: List<ASTNode>,
        private val classes: List<ASTNode>) : Elements {
        override fun sortElements(): MutableList<ASTNode> {
            val (extensionFun, nonExtensionFun) = functions.partition { (it.psi as KtFunction).isExtensionDeclaration() }
            return (properties + listOf(classes, extensionFun, nonExtensionFun).map { nodes ->
                val (privatePart, notPrivatePart) = nodes.partition { it.hasModifier(PRIVATE_KEYWORD) }
                val (protectedPart, notProtectedPart) = notPrivatePart.partition { it.hasModifier(PROTECTED_KEYWORD) || it.hasModifier(OVERRIDE_KEYWORD) }
                val (internalPart, publicPart) = notProtectedPart.partition { it.hasModifier(INTERNAL_KEYWORD) }
                listOf(publicPart, internalPart, protectedPart, privatePart).flatten()
            }.flatten()).toMutableList()
        }
    }

    companion object {
        /**
         * List of children that should be sort
         */
        val sortedType = listOf(PROPERTY, FUN, CLASS)
    }
}
