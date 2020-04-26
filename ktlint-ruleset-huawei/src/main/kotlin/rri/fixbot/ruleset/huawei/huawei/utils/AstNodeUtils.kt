package rri.fixbot.ruleset.huawei.huawei.utils

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

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
