package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOP_LEVEL_ORDER
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.ruleset.utils.isPartOfComment

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_LIST
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.TYPEALIAS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.OVERRIDE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.PROTECTED_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * Rule that checks order in top level
 */
class TopLevelOrderRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(TOP_LEVEL_ORDER),
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            checkNode(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkNode(node: ASTNode) {
        val children = node.getChildren(null)
        val initialElementsOrder = children.filter { it.elementType in sortedType }
        if (initialElementsOrder.isEmpty()) {
            return
        }
        val properties = Properties(children.filter { it.elementType == PROPERTY }).sortElements()
        val functions = children.filter { it.elementType == FUN }
        val typealiases = children.filter { it.elementType == TYPEALIAS }
        val classes = children.filter { it.elementType == CLASS || it.elementType == OBJECT_DECLARATION }
        val sortedElementsWithTrailingNonCodeNodes = Blocks(properties, typealiases, functions, classes).sortElements().map { astNode ->
            astNode to astNode.siblings(false).takeWhile { it.elementType == WHITE_SPACE || it.isPartOfComment() }.toList()
        }
        val lastNonSortedChildren = initialElementsOrder.last().siblings(true).toList()
        sortedElementsWithTrailingNonCodeNodes.filterIndexed { index, pair -> initialElementsOrder[index] != pair.first }
            .forEach { listOfChildren ->
                val wrongNode = listOfChildren.first
                TOP_LEVEL_ORDER.warnAndFix(configRules, emitWarn, isFixMode, wrongNode.text, wrongNode.startOffset, wrongNode) {
                    node.removeRange(node.findChildByType(IMPORT_LIST)!!.treeNext, node.lastChildNode)
                    node.removeChild(node.lastChildNode)
                    sortedElementsWithTrailingNonCodeNodes.map { (sortedNode, sortedNodePrevSibling) ->
                        sortedNodePrevSibling.reversed().map { node.addChild(it, null) }
                        node.addChild(sortedNode, null)
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
     *
     * @param properties
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
     *
     * @param properties
     * @param typealiases
     * @param functions
     * @param classes
     */
    private data class Blocks(
        private val properties: List<ASTNode>,
        private val typealiases: List<ASTNode>,
        private val functions: List<ASTNode>,
        private val classes: List<ASTNode>
    ) : Elements {
        override fun sortElements(): MutableList<ASTNode> {
            val (extensionFun, nonExtensionFun) = functions.partition { (it.psi as KtFunction).isExtensionDeclaration() }
            return (properties + listOf(typealiases, classes, extensionFun, nonExtensionFun).flatMap { nodes ->
                val (privatePart, notPrivatePart) = nodes.partition { it.hasModifier(PRIVATE_KEYWORD) }
                val (protectedPart, notProtectedPart) = notPrivatePart.partition { it.hasModifier(PROTECTED_KEYWORD) || it.hasModifier(OVERRIDE_KEYWORD) }
                val (internalPart, publicPart) = notProtectedPart.partition { it.hasModifier(INTERNAL_KEYWORD) }
                listOf(publicPart, internalPart, protectedPart, privatePart).flatten()
            }).toMutableList()
        }
    }

    companion object {
        const val NAME_ID = "top-level-order"

        /**
         * List of children that should be sort
         */
        val sortedType = listOf(PROPERTY, FUN, CLASS, OBJECT_DECLARATION, TYPEALIAS)
    }
}
