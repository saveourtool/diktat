/**
 * Various utility methods to work with kotlin AST
 * Copied from KtLint for backward compatibility
 */

package org.cqfn.diktat.ruleset.utils

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * @return true if current [ASTNode] is root
 */
fun ASTNode.isRoot(): Boolean = elementType == KtFileElementType.INSTANCE

/**
 * @return true if current [ASTNode] is a leaf
 */
fun ASTNode.isLeaf(): Boolean = firstChildNode == null

fun ASTNode?.isWhiteSpaceWithNewline(): Boolean = this != null && elementType == KtTokens.WHITE_SPACE && textContains('\n')

/**
 * @param strict true if it needs to check current [ASTNode]
 * @param predicate
 * @return first parent which meets [predicate] condition
 */
fun ASTNode.parent(
    strict: Boolean = true,
    predicate: (ASTNode) -> Boolean,
): ASTNode? = if (!strict && predicate(this)) {
    this
} else {
    parents().firstOrNull(predicate)
}

/**
 * @param elementType [IElementType]
 */
fun ASTNode.parent(
    elementType: IElementType,
    strict: Boolean = true,
): ASTNode? = parent(strict) { it.elementType == elementType }

/**
 * @return true if current [ASTNode] is [KtTokens.WHITE_SPACE]
 */
fun ASTNode?.isWhiteSpace(): Boolean = this?.elementType == KtTokens.WHITE_SPACE

/**
 * @return true if current [ASTNode] is not part of a comment
 */
fun ASTNode.isPartOfComment(): Boolean = parent(strict = false) { it.psi is PsiComment } != null

/**
 * @return previous sibling [ASTNode] which is code
 */
fun ASTNode.prevCodeSibling(): ASTNode? = prevSibling { !it.isNotCode() }

/**
 * @param predicate
 * @return previous sibling [ASTNode] which matches [predicate]
 */
inline fun ASTNode.prevSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? = siblings(false).firstOrNull(predicate)

/**
 * @return next sibling [ASTNode] which is code
 */
fun ASTNode.nextCodeSibling(): ASTNode? = nextSibling { !it.isNotCode() }

inline fun ASTNode.nextSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? = siblings(true).firstOrNull(predicate)

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

/**
 * @param includeEmpty
 * @param skipSubtree
 * @return next [ASTNode] which is a code leaf
 */
fun ASTNode.nextCodeLeaf(
    includeEmpty: Boolean = false,
    skipSubtree: Boolean = false,
): ASTNode? = generateSequence(nextLeaf(includeEmpty, skipSubtree)) {
    it.nextLeaf(includeEmpty, skipSubtree)
}
    .firstOrNull {
        it.isNotCode()
    }

private fun ASTNode.isNotCode(): Boolean = isWhiteSpace() || isPartOfComment()

/**
 * @param elementType
 * @return true if current [ASTNode] has a parent with [elementType]
 */
fun ASTNode.isPartOf(elementType: IElementType): Boolean = parent(elementType, strict = false) != null
