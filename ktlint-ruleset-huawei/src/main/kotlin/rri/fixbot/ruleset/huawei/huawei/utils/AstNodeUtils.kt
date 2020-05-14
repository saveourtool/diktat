package rri.fixbot.ruleset.huawei.huawei.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.isLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger(ASTNode::class.java)

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


/**
 *
 */
fun ASTNode.findChildBefore(beforeThisNodeType: IElementType, childNodeType: IElementType) =
    this.findChildAfter(childNodeType, beforeThisNodeType)

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

    log.warn("Not able to find a node with type ${childNodeType} after ${afterThisNodeType}")
    return null
}

// applicable for PROPERTY element type only
fun ASTNode.isNodeFromCompanionObject(): Boolean {
    val parent = this.treeParent
    if (parent.elementType == ElementType.CLASS_BODY) {
        if (parent.treeParent.elementType == ElementType.OBJECT_DECLARATION) {
            return true;
        }
    }
    return false
}

fun ASTNode.isNodeFromFileLevel(): Boolean = this.treeParent.elementType == FILE

fun ASTNode.isValProperty() =
    this.getChildren(null)
        .any { it.elementType == ElementType.VAL_KEYWORD }

fun ASTNode.isVarProperty() =
    this.getChildren(null)
        .any { it.elementType == ElementType.VAR_KEYWORD }

/**
 * This util method does tree traversal and stores to the result all tree leaf node of particular type (elementType).
 * Recursively will visit each and every node and will get leafs of specific type. Those nodes will be added to the result.
 */
fun ASTNode.getAllLLeafsWithSpecificType(elementType: IElementType, result: MutableList<ASTNode>) {
    // if statements here have the only right order - don't change it
    if (this.isLeaf()) {
        if (this.elementType == elementType) {
            result.add(this)
        }
    } else {
        this.getChildren(null).forEach {
            it.getAllLLeafsWithSpecificType(elementType, result)
        }
    }
}
