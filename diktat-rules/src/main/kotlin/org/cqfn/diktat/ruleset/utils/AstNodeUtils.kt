package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PUBLIC_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger(ASTNode::class.java)
val emptyBlockList = listOf(ElementType.LBRACE, WHITE_SPACE, ElementType.RBRACE)

fun ASTNode.checkLength(range: IntRange): Boolean = this.textLength in range

/**
 * getting first child name with IDENTIFIER type
 */
fun ASTNode.getIdentifierName(): ASTNode? =
        this.getChildren(null).find { it.elementType == ElementType.IDENTIFIER }

/**
 * getting first child name with TYPE_PARAMETER_LIST type
 */
fun ASTNode.getTypeParameterList(): ASTNode? =
        this.getChildren(null).find { it.elementType == ElementType.TYPE_PARAMETER_LIST }

/**
 * getting all children that have IDENTIFIER type
 */
fun ASTNode.getAllIdentifierChildren(): List<ASTNode> =
        this.getChildren(null).filter { it.elementType == ElementType.IDENTIFIER }


/**
 * obviously returns list with children that match particular element type
 */
fun ASTNode.getAllChildrenWithType(elementType: IElementType): List<ASTNode> =
        this.getChildren(null).filter { it.elementType == elementType }

/**
 * obviously returns first child that match particular element type
 */
fun ASTNode.getFirstChildWithType(elementType: IElementType): ASTNode? =
        this.getChildren(null).find { it.elementType == elementType }

fun ASTNode.isFollowedByNewline() =
        this.treeNext.elementType == WHITE_SPACE && this.treeNext.text.contains("\n")

fun ASTNode.isBeginByNewline() =
        this.treePrev.elementType == WHITE_SPACE && this.treePrev.text.contains("\n")
/**
 * checks if the node has corresponding child with elementTyp
 */
fun ASTNode.hasChildOfType(elementType: IElementType): Boolean =
        this.getFirstChildWithType(elementType) != null

fun ASTNode.hasAnyChildOfTypes(vararg elementType: IElementType): Boolean =
        elementType.any { this.hasChildOfType(it) }

/**
 * check if node's block is empty (contains only left and right braces and space)
 */
fun ASTNode?.isBlockEmpty() = this?.let { emptyBlockList
        .containsAll(this.getChildren(null).map { it.elementType })} ?: true
/**
 * Method that is trying to find and return child of this node, which
 * 1) stands before the node with type @beforeThisNodeType
 * 2) has type @childNodeType
 * 2) is closest to node with @beforeThisNodeType (i.e. is first in reversed list of children, starting at @beforeThisNodeType)
 */
fun ASTNode.findChildBefore(beforeThisNodeType: IElementType, childNodeType: IElementType): ASTNode? {
    val anchorNode = getChildren(null).find { it.elementType == beforeThisNodeType }
    getChildren(null).toList().let {
        if (anchorNode != null)
            it.subList(0, it.indexOf(anchorNode))
        else it
    }.reversed()
            .find { it.elementType == childNodeType }
            ?.let { return it }

    log.warn("Not able to find a node with type $childNodeType before $beforeThisNodeType")
    return null
}

/**
 * method that is trying to find and return FIRST node that matches these conditions:
 * 1) it is one of children of "this"
 * 2) it stands in the list of children AFTER the node with type @afterThisNodeType
 * 3) it has type @childNodeType
 */
fun ASTNode.findChildAfter(afterThisNodeType: IElementType, childNodeType: IElementType): ASTNode? {
    var foundAnchorNode = false
    getChildren(null).forEach {
        // if we have already found previous node and type matches - then can return child
        if (foundAnchorNode && it.elementType == childNodeType) return it
        // found the node that is used as anchor and we are trying to find
        // a node with IElementType that stands after this anchor node
        if (it.elementType == afterThisNodeType) {
            foundAnchorNode = true
        }
    }

    log.warn("Not able to find a node with type $childNodeType after $afterThisNodeType")
    return null
}

fun ASTNode.allSiblings(withSelf: Boolean = false): List<ASTNode> =
        siblings(false).toList() + (if (withSelf) listOf(this) else listOf()) + siblings(true)

fun ASTNode.isNodeFromCompanionObject(): Boolean {
    val parent = this.treeParent
    if (parent != null) {
        val grandParent = parent.treeParent
        if (grandParent != null && grandParent.elementType == ElementType.OBJECT_DECLARATION) {
            if (grandParent.findLeafWithSpecificType(ElementType.COMPANION_KEYWORD) != null) {
                return true;
            }
        }
    }
    return false
}

fun ASTNode.isConstant(): Boolean {
    return (this.isNodeFromFileLevel() || this.isNodeFromObject()) && this.isValProperty() && this.isConst()
}

fun ASTNode.isNodeFromObject(): Boolean {
    val parent = this.treeParent
    if (parent != null && parent.elementType == ElementType.CLASS_BODY) {
        val grandParent = parent.treeParent
        if (grandParent != null && grandParent.elementType == ElementType.OBJECT_DECLARATION) {
            return true;
        }
    }
    return false
}

fun ASTNode.isNodeFromFileLevel(): Boolean = this.treeParent.elementType == FILE

fun ASTNode.isValProperty() =
        this.getChildren(null)
                .any { it.elementType == ElementType.VAL_KEYWORD }

fun ASTNode.isConst() = this.findLeafWithSpecificType(CONST_KEYWORD) != null

fun ASTNode.isVarProperty() =
        this.getChildren(null)
                .any { it.elementType == ElementType.VAR_KEYWORD }

fun ASTNode.toLower() {
    (this as LeafPsiElement).replaceWithText(this.text.toLowerCase())
}

/**
 * This util method does tree traversal and stores to the result all tree leaf node of particular type (elementType).
 * Recursively will visit each and every node and will get leafs of specific type. Those nodes will be added to the result.
 */
fun ASTNode.getAllLeafsWithSpecificType(elementType: IElementType, result: MutableList<ASTNode>) {
    // if statements here have the only right order - don't change it
    if (this.isLeaf()) {
        if (this.elementType == elementType) {
            result.add(this)
        }
    } else {
        this.getChildren(null).forEach {
            it.getAllLeafsWithSpecificType(elementType, result)
        }
    }
}

/**
 * This util method does tree traversal and returns first node that matches specific type
 * This node isn't necessarily a leaf though method name implies it
 */
fun ASTNode.findLeafWithSpecificType(elementType: IElementType): ASTNode? {
    if (this.elementType == elementType) return this
    if (this.isLeaf()) return null

    this.getChildren(null).forEach {
        val result = it.findLeafWithSpecificType(elementType)
        if (result != null) return result
    }
    return null
}

/**
 * This method performs tree traversal and returns all nodes with specific element type
 */
fun ASTNode.findAllNodesWithSpecificType(elementType: IElementType): List<ASTNode> {
    val initialAcc = if (this.elementType == elementType) mutableListOf(this) else mutableListOf()
    return initialAcc + this.getChildren(null).flatMap {
        it.findAllNodesWithSpecificType(elementType)
    }
}

/**
 * This method finds first parent node from the sequence of parents that has specified elementType
 */
fun ASTNode.findParentNodeWithSpecificType(elementType: IElementType) =
        this.parents().find { it.elementType == elementType }

/**
 * Finds all children of optional type which match the predicate
 */
fun ASTNode.findChildrenMatching(elementType: IElementType? = null, predicate: (ASTNode) -> Boolean): List<ASTNode> =
        getChildren(elementType?.let { TokenSet.create(it) })
                .filter(predicate)

/**
 * Check if this node has any children of optional type matching the predicate
 */
fun ASTNode.hasChildMatching(elementType: IElementType? = null, predicate: (ASTNode) -> Boolean): Boolean =
        findChildrenMatching(elementType, predicate).isNotEmpty()

/**
 * Converts this AST node and all its children to pretty string representation
 */
fun ASTNode.prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
    // AST operates with \n only, so we need to build the whole string representation and then change line separator
    fun ASTNode.doPrettyPrint(level: Int, maxLevel: Int): String {
        val result = StringBuilder("${this.elementType}: \"${this.text}\"").append('\n')
        if (maxLevel != 0) {
            this.getChildren(null).forEach { child ->
                result.append("${"-".repeat(level + 1)} " +
                        child.doPrettyPrint(level + 1, maxLevel - 1))
            }
        }
        return result.toString()
    }
    return doPrettyPrint(level, maxLevel).replace("\n", System.lineSeparator())
}

/**
 * Checks if this modifier list corresponds to accessible outside entity
 * @param modifierList ASTNode with ElementType.MODIFIER_LIST, can be null if entity has no modifier list
 */
fun ASTNode?.isAccessibleOutside(): Boolean =
        if (this != null) {
            assert(this.elementType == MODIFIER_LIST)
            this.hasAnyChildOfTypes(PUBLIC_KEYWORD, PROTECTED_KEYWORD, INTERNAL_KEYWORD) ||
                    !this.hasAnyChildOfTypes(PUBLIC_KEYWORD, INTERNAL_KEYWORD, PROTECTED_KEYWORD, PRIVATE_KEYWORD)
        } else {
            true
        }

/**
 * removing all newlines in WHITE_SPACE node and replacing it to a one newline saving the initial indenting format
 */
fun ASTNode.leaveOnlyOneNewLine() = leaveExactlyNumNewLines(1)

/**
 * removing all newlines in WHITE_SPACE node and replacing it to [num] newlines saving the initial indenting format
 */
fun ASTNode.leaveExactlyNumNewLines(num: Int) {
    require(this.elementType == WHITE_SPACE)
    (this as LeafPsiElement).replaceWithText("${"\n".repeat(num)}${this.text.replace("\n", "")}")
}

/**
 * Transforms last line of this WHITE_SPACE to exactly [indent] spaces
 */
fun ASTNode.indentBy(indent: Int) {
    require(this.elementType == WHITE_SPACE)
    (this as LeafPsiElement).rawReplaceWithText(text.substringBeforeLast('\n') + "\n" + " ".repeat(indent))
}

/**
 * @param beforeThisNode node before which childToMove will be placed. If null, childToMove will be appended after last child of this node.
 * @param withNextNode whether next node after childToMove should be moved too. In most cases it corresponds to moving
 *     the node with newline.
 */
fun ASTNode.moveChildBefore(childToMove: ASTNode, beforeThisNode: ASTNode?, withNextNode: Boolean = false): ReplacementResult {
    require(childToMove in getChildren(null)) { "can only move child of this node" }
    require(beforeThisNode?.let { it in getChildren(null) }
            ?: true) { "can only place node before another child of this node" }
    val movedChild = childToMove.clone() as ASTNode
    val nextMovedChild = childToMove.treeNext?.let { it.clone() as ASTNode }?.takeIf { withNextNode }
    val nextOldChild = childToMove.treeNext.takeIf { withNextNode && it != null }
    addChild(movedChild, beforeThisNode)
    if (nextMovedChild != null) {
        addChild(nextMovedChild, beforeThisNode)
        removeChild(nextOldChild!!)
    }
    removeChild(childToMove)
    return ReplacementResult(listOfNotNull(childToMove, nextOldChild), listOfNotNull(movedChild, nextMovedChild))
}

fun ASTNode.findLBrace():ASTNode? {
    return when (this.elementType) {
        ElementType.THEN, ElementType.ELSE -> this.findChildByType(ElementType.BLOCK)?.findChildByType(ElementType.LBRACE)!!
        ElementType.WHEN -> this.findChildByType(ElementType.LBRACE)!!
        ElementType.FOR, ElementType.WHILE, ElementType.DO_WHILE ->
            this.findChildByType(ElementType.BODY)?.findChildByType(ElementType.BLOCK)?.findChildByType(ElementType.LBRACE)!!
        ElementType.CLASS, ElementType.OBJECT_DECLARATION -> this.findChildByType(ElementType.CLASS_BODY)!!.findChildByType(ElementType.LBRACE)!!
        else -> if (this.hasChildOfType(ElementType.BLOCK)) this.findChildByType(ElementType.BLOCK)?.findChildByType(ElementType.LBRACE)!! else null
    }
}

fun ASTNode.isChildAfterAnother(child: ASTNode, afterChild: ASTNode): Boolean =
        getChildren(null).indexOf(child) > getChildren(null).indexOf(afterChild)

fun ASTNode.isChildAfterGroup(child: ASTNode, group: List<ASTNode>): Boolean =
        getChildren(null).indexOf(child) > (group.map { getChildren(null).indexOf(it) }.max() ?: 0)

fun ASTNode.isChildBeforeAnother(child: ASTNode, beforeChild: ASTNode): Boolean = areChildrenBeforeGroup(listOf(child), listOf(beforeChild))
fun ASTNode.isChildBeforeGroup(child: ASTNode, group: List<ASTNode>): Boolean = areChildrenBeforeGroup(listOf(child), group)
fun ASTNode.areChildrenBeforeChild(children: List<ASTNode>, beforeChild: ASTNode): Boolean = areChildrenBeforeGroup(children, listOf(beforeChild))
fun ASTNode.areChildrenBeforeGroup(children: List<ASTNode>, group: List<ASTNode>): Boolean {
    require(children.isNotEmpty() && group.isNotEmpty()) { "no sense to operate on empty lists" }
    return children.map { getChildren(null).indexOf(it) }.max()!! < group.map { getChildren(null).indexOf(it) }.min()!!
}

fun List<ASTNode>.handleIncorrectOrder(getSiblingBlocks: ASTNode.() -> Pair<ASTNode?, ASTNode>,
                                       incorrectPositionHandler: (nodeToMove: ASTNode, beforeThisNode: ASTNode) -> Unit) {
    forEach { astNode ->
        val (afterThisNode, beforeThisNode) = astNode.getSiblingBlocks()
        val isPositionIncorrect = (afterThisNode != null && !astNode.treeParent.isChildAfterAnother(astNode, afterThisNode))
                || !astNode.treeParent.isChildBeforeAnother(astNode, beforeThisNode)

        if (isPositionIncorrect) incorrectPositionHandler(astNode, beforeThisNode)
    }
}

data class ReplacementResult(val oldNodes: List<ASTNode>, val newNodes: List<ASTNode>) {
    init {
        require(oldNodes.size == newNodes.size)
    }
}
