package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.COMPANION_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LATEINIT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.parent
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BLANK_LINE_BETWEEN_PROPERTIES
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.handleIncorrectOrder
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.cqfn.diktat.ruleset.utils.leaveExactlyNumNewLines
import org.cqfn.diktat.ruleset.utils.loggerPropertyRegex
import org.cqfn.diktat.ruleset.utils.moveChildBefore
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule that checks order of declarations inside classes, interfaces and objects.
 */
class ClassLikeStructuresOrderRule(private val configRules: List<RulesConfig>) : Rule("class-like-structures") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)

    /**
     * @param node
     * @param autoCorrect
     * @param emit
     */
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS_BODY) {
            checkDeclarationsOrderInClass(node)
        } else if (node.elementType == PROPERTY) {
            checkNewLinesBeforeProperty(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkDeclarationsOrderInClass(node: ASTNode) {
        val allProperties = node.getChildren(TokenSet.create(PROPERTY))
        val constProperties = allProperties.filter { it.findLeafWithSpecificType(CONST_KEYWORD) != null }.toMutableList()
        val lateInitProperties = allProperties.filter { it.findLeafWithSpecificType(LATEINIT_KEYWORD) != null }.toMutableList()
        val loggers = allProperties
                .filter {
                    (it.findChildByType(MODIFIER_LIST) == null || it.findLeafWithSpecificType(PRIVATE_KEYWORD) != null) &&
                            it.getIdentifierName()!!.text.contains(loggerPropertyRegex)
                }
                .toMutableList()
        val properties = allProperties.filter { it !in lateInitProperties && it !in loggers && it !in constProperties }.toMutableList()
        val initBlocks = node.getChildren(TokenSet.create(CLASS_INITIALIZER)).toMutableList()
        val constructors = node.getChildren(TokenSet.create(SECONDARY_CONSTRUCTOR)).toMutableList()
        val methods = node.getChildren(TokenSet.create(FUN)).toMutableList()
        val (usedClasses, unusedClasses) = node
                .getChildren(TokenSet.create(CLASS))
                .partition { classNode ->
                    classNode.getIdentifierName()?.let { identifierNode ->
                        node
                                .parents()
                                .last()
                                .findAllNodesWithSpecificType(REFERENCE_EXPRESSION)
                                .any { ref ->
                                    ref.parent({ it == classNode }) == null && ref.text.contains(identifierNode.text)
                                }
                    } ?: false
                }
                .let { it.first.toMutableList() to it.second.toMutableList() }
        val companion = node.getChildren(TokenSet.create(OBJECT_DECLARATION))
                .find { it.findChildByType(MODIFIER_LIST)?.findLeafWithSpecificType(COMPANION_KEYWORD) != null }
        val blocks = Blocks(AllProperties(loggers, constProperties, properties, lateInitProperties),
                initBlocks, constructors, methods, usedClasses, listOfNotNull(companion).toMutableList(),
                unusedClasses)

        blocks
                .allBlockFlattened()
                .reversed()
                .handleIncorrectOrder(blocks::getSiblingBlocks) { astNode, beforeThisNode ->
                    WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnAndFix(configRules, emitWarn, isFixMode, "${astNode.elementType}: ${astNode.text}", astNode.startOffset, astNode) {
                        val replacement = node.moveChildBefore(astNode, beforeThisNode, true)
                        replacement.oldNodes.forEachIndexed { idx, oldNode ->
                            blocks
                                    .allBlocks()
                                    .find { oldNode in it }
                                    ?.apply {
                                        this[indexOf(oldNode)] = replacement.newNodes[idx]
                                    }
                        }
                    }
                }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkNewLinesBeforeProperty(node: ASTNode) {
        // checking only top-level and class-level properties
        if (node.treeParent.elementType != CLASS_BODY) {
            return
        }

        val previousProperty = node.prevSibling { it.elementType == PROPERTY }

        if (previousProperty != null) {
            val hasCommentBefore = node
                    .findChildByType(TokenSet.create(KDOC, EOL_COMMENT, BLOCK_COMMENT))
                    ?.isFollowedByNewline()
                    ?: false
            val hasAnnotationsBefore = (node.psi as KtProperty)
                    .annotationEntries
                    .any { it.node.isFollowedByNewline() }
            val hasCustomAccessors = (node.psi as KtProperty).accessors.isNotEmpty() ||
                    (previousProperty.psi as KtProperty).accessors.isNotEmpty()

            val whiteSpaceBefore = previousProperty.nextSibling { it.elementType == WHITE_SPACE } ?: return
            val isBlankLineRequired = hasCommentBefore || hasAnnotationsBefore || hasCustomAccessors
            val numRequiredNewLines = 1 + (if (isBlankLineRequired) 1 else 0)
            val actualNewLines = whiteSpaceBefore.text.count { it == '\n' }
            // for some cases (now - if this or previous property has custom accessors), blank line is allowed before it
            if (!hasCustomAccessors && actualNewLines != numRequiredNewLines ||
                    hasCustomAccessors && actualNewLines > numRequiredNewLines
            ) {
                BLANK_LINE_BETWEEN_PROPERTIES.warnAndFix(configRules, emitWarn, isFixMode, node.getIdentifierName()!!.text, node.startOffset, node) {
                    whiteSpaceBefore.leaveExactlyNumNewLines(numRequiredNewLines)
                }
            }
        }
    }

    /**
     * Data class containing different groups of properties in file
     *
     * @property loggers loggers (for example, properties called `log` or `logger`)
     * @property constProperties `const val`s
     * @property properties all other properties
     * @property lateInitProperties `lateinit var`s
     */
    private data class AllProperties(val loggers: MutableList<ASTNode>,
                                     val constProperties: MutableList<ASTNode>,
                                     val properties: MutableList<ASTNode>,
                                     val lateInitProperties: MutableList<ASTNode>)

    /**
     * @property allProperties an instance of [AllProperties]
     * @property initBlocks `init` blocks
     * @property constructors constructors
     * @property methods functions
     * @property usedClasses nested classes that are used in the enclosing class
     * @property companion `companion object`s
     * @property unusedClasses nested classes that are *not* used in the enclosing class
     */
    private data class Blocks(val allProperties: AllProperties,
                              val initBlocks: MutableList<ASTNode>,
                              val constructors: MutableList<ASTNode>,
                              val methods: MutableList<ASTNode>,
                              val usedClasses: MutableList<ASTNode>,
                              val companion: MutableList<ASTNode>,
                              val unusedClasses: MutableList<ASTNode>) {
        init {
            require(companion.size in 0..1)
        }

        /**
         * @return all groups of structures in the class
         */
        fun allBlocks() = with(allProperties) {
            listOf(loggers, constProperties, properties, lateInitProperties,
                    initBlocks, constructors, methods, usedClasses, companion, unusedClasses)
        }

        fun allBlockFlattened() = allBlocks().flatten()

        /**
         * @param node
         * @return nodes between which the [node] should be placed
         */
        fun getSiblingBlocks(node: ASTNode): Pair<ASTNode?, ASTNode> {
            require(node in allBlockFlattened())
            val lastElement = allBlockFlattened().last()
            val idx = allBlocks().indexOfFirst { node in it }
            return (allBlocks().subList(0, idx) to allBlocks().subList(idx + 1, allBlocks().size))
                    .let { it.first.flatten() to it.second.flatten() }
                    .let { it.first.firstOrNull() to (it.second.firstOrNull() ?: lastElement.treeNext) }
        }
    }
}
