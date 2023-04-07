/**
 * Various utility methods to work with kotlin AST
 * Copied from KtLint for backward compatibility
 */

package org.cqfn.diktat.ruleset.utils

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType


/// KtLint's extensions

fun ASTNode.isRoot(): Boolean = elementType == KtFileElementType.INSTANCE

fun ASTNode.isLeaf(): Boolean = firstChildNode == null

fun ASTNode?.isWhiteSpaceWithNewline(): Boolean = this != null && elementType == KtTokens.WHITE_SPACE && textContains('\n')

fun ASTNode.parent(
    strict: Boolean = true,
    predicate: (ASTNode) -> Boolean,
): ASTNode? {
    var n: ASTNode? = if (strict) this.treeParent else this
    while (n != null) {
        if (predicate(n)) {
            return n
        }
        n = n.treeParent
    }
    return null
}

/**
 * @param elementType [IElementType]
 */
fun ASTNode.parent(
    elementType: IElementType,
    strict: Boolean = true,
): ASTNode? = parent(strict) { it.elementType == elementType }

fun ASTNode?.isWhiteSpace(): Boolean = this != null && elementType == KtTokens.WHITE_SPACE

fun ASTNode.isPartOfComment(): Boolean = parent(strict = false) { it.psi is PsiComment } != null

fun ASTNode.prevCodeSibling(): ASTNode? = prevSibling { it.elementType != KtTokens.WHITE_SPACE && !it.isPartOfComment() }

inline fun ASTNode.prevSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? {
    var n = this.treePrev
    while (n != null) {
        if (predicate(n)) {
            return n
        }
        n = n.treePrev
    }
    return null
}

fun ASTNode.nextCodeSibling(): ASTNode? = nextSibling { it.elementType != KtTokens.WHITE_SPACE && !it.isPartOfComment() }

inline fun ASTNode.nextSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? {
    var n = this.treeNext
    while (n != null) {
        if (predicate(n)) {
            return n
        }
        n = n.treeNext
    }
    return null
}

private fun ASTNode.nextLeaf(
    includeEmpty: Boolean = false,
    skipSubtree: Boolean = false,
): ASTNode? {
    var n = if (skipSubtree) this.lastChildLeafOrSelf().nextLeafAny() else this.nextLeafAny()
    if (!includeEmpty) {
        while (n != null && n.textLength == 0) {
            n = n.nextLeafAny()
        }
    }
    return n
}

private fun ASTNode.lastChildLeafOrSelf(): ASTNode {
    var n = this
    if (n.lastChildNode != null) {
        do {
            n = n.lastChildNode
        } while (n.lastChildNode != null)
        return n
    }
    return n
}

private fun ASTNode.nextLeafAny(): ASTNode? {
    var n = this
    if (n.firstChildNode != null) {
        do {
            n = n.firstChildNode
        } while (n.firstChildNode != null)
        return n
    }
    return n.nextLeafStrict()
}

private fun ASTNode.nextLeafStrict(): ASTNode? {
    val nextSibling: ASTNode? = treeNext
    if (nextSibling != null) {
        return nextSibling.firstChildLeafOrSelf()
    }
    return treeParent?.nextLeafStrict()
}

private fun ASTNode.firstChildLeafOrSelf(): ASTNode {
    var n = this
    if (n.firstChildNode != null) {
        do {
            n = n.firstChildNode
        } while (n.firstChildNode != null)
        return n
    }
    return n
}

fun ASTNode.nextCodeLeaf(
    includeEmpty: Boolean = false,
    skipSubtree: Boolean = false,
): ASTNode? {
    var n = nextLeaf(includeEmpty, skipSubtree)
    while (n != null && (n.elementType == KtTokens.WHITE_SPACE || n.isPartOfComment())) {
        n = n.nextLeaf(includeEmpty, skipSubtree)
    }
    return n
}

fun ASTNode.isPartOf(elementType: IElementType): Boolean = parent(elementType, strict = false) != null
