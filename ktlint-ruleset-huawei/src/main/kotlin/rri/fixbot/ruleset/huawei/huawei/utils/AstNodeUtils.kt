package rri.fixbot.ruleset.huawei.huawei.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.isLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

fun ASTNode.checkLength(range: IntRange): Boolean = this.textLength in range

fun ASTNode.getIdentifierName(): ASTNode? =
    this.getChildren(null)
        .find { it.elementType == ElementType.IDENTIFIER }


// applicable for PROPERTY element type only
fun ASTNode.isVariableFromCompanionObject(): Boolean {
    val parent = this.treeParent
    if (parent.elementType == ElementType.CLASS_BODY) {
        if (parent.treeParent.elementType == ElementType.OBJECT_DECLARATION) {
            return true;
        }
    }
    return false
}

fun ASTNode.isValProperty() =
    this.getChildren(null)
        .any { it.elementType == ElementType.VAL_KEYWORD }

fun ASTNode.isVarProperty() =
    this.getChildren(null)
        .any { it.elementType == ElementType.VAR_KEYWORD }

/**
 * This util method does tree traversal and stores to the result all tree leaf node of particular type (elementType)
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
