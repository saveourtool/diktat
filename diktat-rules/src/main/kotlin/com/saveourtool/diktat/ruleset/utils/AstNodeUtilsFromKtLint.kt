/**
 * Various utility methods to work with kotlin AST
 * Copied from KtLint for backward compatibility
 */

package com.saveourtool.diktat.ruleset.utils

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

/**
 * @return true if this [ASTNode] is [KtTokens.WHITE_SPACE] and contains '\n'
 */
fun ASTNode?.isWhiteSpaceWithNewline(): Boolean = this?.elementType == KtTokens.WHITE_SPACE && this?.textContains('\n') == true

/**
 * @param strict true if it doesn't need to check current [ASTNode]
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
 * @param strict
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
fun ASTNode.prevCodeSibling(): ASTNode? = prevSibling { it.isCode() }

/**
 * @param predicate
 * @return previous sibling [ASTNode] which matches [predicate]
 */
inline fun ASTNode.prevSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? = siblings(false).firstOrNull(predicate)

/**
 * @return next sibling [ASTNode] which is code
 */
fun ASTNode.nextCodeSibling(): ASTNode? = nextSibling { it.isCode() }

/**
 * @param predicate
 * @return [ASTNode] next sibling which matches [predicate]
 */
inline fun ASTNode.nextSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? = siblings(true).firstOrNull(predicate)

/**
 * @return next [ASTNode] which is a code leaf
 */
fun ASTNode.nextCodeLeaf(): ASTNode? = generateSequence(nextLeaf()) { it.nextLeaf() }
    .firstOrNull {
        it.isCode()
    }

/**
 * @param elementType
 * @return true if current [ASTNode] has a parent with [elementType]
 */
fun ASTNode.isPartOf(elementType: IElementType): Boolean = parent(elementType, strict = false) != null

private fun ASTNode.nextLeaf(): ASTNode? = generateSequence(nextLeafAny()) { it.nextLeafAny() }
    .firstOrNull { it.textLength != 0 }

private fun ASTNode.nextLeafAny(): ASTNode? = firstChildLeaf() ?: nextLeafStrict()

private fun ASTNode.nextLeafStrict(): ASTNode? = treeNext?.firstChildLeafOrSelf() ?: treeParent?.nextLeafStrict()

private fun ASTNode.firstChildLeafOrSelf(): ASTNode = firstChildLeaf() ?: this

private fun ASTNode.firstChildLeaf(): ASTNode? = generateSequence(firstChildNode, ASTNode::getFirstChildNode)
    .lastOrNull()

private fun ASTNode.isCode(): Boolean = !isWhiteSpace() && !isPartOfComment()
