/**
 * Various utility methods to work with KDoc representation in AST
 */

package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

/**
 * @return a list of [KDocTag]s from this KDoc node
 */
fun ASTNode.kDocTags(): List<KDocTag> {
    require(this.elementType == ElementType.KDOC) { "kDoc tags can be retrieved only from KDOC node" }
    return this.getAllChildrenWithType(KDOC_SECTION).flatMap { sectionNode ->
        sectionNode.getAllChildrenWithType(ElementType.KDOC_TAG)
            .map { its -> its.psi as KDocTag }
    }
}

/**
 * @param knownTag a tag to look for
 * @return whether this tag is present
 */
fun Iterable<KDocTag>.hasKnownKdocTag(knownTag: KDocKnownTag): Boolean =
    this.find { it.knownTag == knownTag } != null

/**
 * Checks for trailing newlines in tag's body. Handles cases, when there is no leading asterisk on an empty line:
 * ```
 * * @param param
 *
 * * @return
 * ```
 * as well as usual simple cases.
 *
 * @return true if there is a trailing newline
 */
fun KDocTag.hasTrailingNewlineInTagBody() = node.lastChildNode.isWhiteSpaceWithNewline() ||
    node.reversedChildren()
        .takeWhile { it.elementType == WHITE_SPACE || it.elementType == ElementType.KDOC_LEADING_ASTERISK }
        .firstOrNull { it.elementType == ElementType.KDOC_LEADING_ASTERISK }
        ?.takeIf { it.treeNext == null || it.treeNext.elementType == WHITE_SPACE } != null

/**
 * This method inserts a new tag into KDoc before specified another tag, aligning it with the rest of this KDoc
 *
 * @param beforeTag tag before which the new one will be placed
 * @param consumer lambda which should be used to fill new tag with data, accepts CompositeElement as an argument
 */
@Suppress("UnsafeCallOnNullableType")
inline fun ASTNode.insertTagBefore(
    beforeTag: ASTNode?,
    consumer: CompositeElement.() -> Unit
) {
    require(this.elementType == ElementType.KDOC && this.hasChildOfType(KDOC_SECTION)) { "kDoc tags can be inserted only into KDOC node" }
    val kdocSection = this.getFirstChildWithType(KDOC_SECTION)!!
    val newTag = CompositeElement(ElementType.KDOC_TAG)
    val beforeTagLineStart = beforeTag?.prevSibling {
        it.elementType == WHITE_SPACE && it.treeNext?.elementType == ElementType.KDOC_LEADING_ASTERISK
    }
    val indent = this
        .getFirstChildWithType(WHITE_SPACE)
        ?.text
        ?.split("\n")
        ?.last() ?: ""
    kdocSection.addChild(PsiWhiteSpaceImpl("\n$indent"), beforeTagLineStart)
    kdocSection.addChild(LeafPsiElement(ElementType.KDOC_LEADING_ASTERISK, "*"), beforeTagLineStart)
    kdocSection.addChild(LeafPsiElement(ElementType.KDOC_TEXT, " "), beforeTagLineStart)
    kdocSection.addChild(newTag, beforeTagLineStart)
    consumer(newTag)
}
