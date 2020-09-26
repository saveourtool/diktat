package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

fun ASTNode.kDocTags(): List<KDocTag>? {
    require(this.elementType == ElementType.KDOC) { "kDoc tags can be retrieved only from KDOC node" }
    return this.getAllChildrenWithType(KDOC_SECTION)
            .map { sectionNode ->
                sectionNode.getAllChildrenWithType(ElementType.KDOC_TAG)
                        .map { its -> its.psi as KDocTag }
            }
            .flatten()
}

fun Iterable<KDocTag>.hasKnownKDocTag(knownTag: KDocKnownTag): Boolean =
        this.find { it.knownTag == knownTag } != null

/**
 * This method inserts a new tag into KDoc before specified another tag, aligning it with the rest of this KDoc
 * @param beforeTag tag before which the new one will be placed
 * @param consumer lambda which should be used to fill new tag with data, accepts CompositeElement as an argument
 */
@Suppress("UnsafeCallOnNullableType")
inline fun ASTNode.insertTagBefore(beforeTag: ASTNode?,
                                   consumer: CompositeElement.() -> Unit) {
    require(this.elementType == ElementType.KDOC && this.hasChildOfType(KDOC_SECTION)) { "kDoc tags can be inserted only into KDOC node" }
    val kDocSection = this.getFirstChildWithType(KDOC_SECTION)!!
    val newTag = CompositeElement(ElementType.KDOC_TAG)
    val beforeTagLineStart = beforeTag?.prevSibling {
        it.elementType == WHITE_SPACE && it.treeNext?.elementType == ElementType.KDOC_LEADING_ASTERISK
    }
    val indent = this.getFirstChildWithType(WHITE_SPACE)?.text?.split("\n")?.last() ?: ""
    kDocSection.addChild(PsiWhiteSpaceImpl("\n$indent"), beforeTagLineStart)
    kDocSection.addChild(LeafPsiElement(ElementType.KDOC_LEADING_ASTERISK, "*"), beforeTagLineStart)
    kDocSection.addChild(LeafPsiElement(ElementType.KDOC_TEXT, " "), beforeTagLineStart)
    kDocSection.addChild(newTag, beforeTagLineStart)
    consumer(newTag)
}
