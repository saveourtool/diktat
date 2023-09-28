package com.saveourtool.diktat.ruleset.rules.chapter2.kdoc

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_EMPTY_KDOC
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_DEPRECATED_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WRONG_SPACES_AFTER_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WRONG_TAGS_ORDER
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.allSiblings
import com.saveourtool.diktat.ruleset.utils.findChildAfter
import com.saveourtool.diktat.ruleset.utils.findChildBefore
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.getIdentifierName
import com.saveourtool.diktat.ruleset.utils.hasChildMatching
import com.saveourtool.diktat.ruleset.utils.hasTrailingNewlineInTagBody
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline
import com.saveourtool.diktat.ruleset.utils.kDocTags
import com.saveourtool.diktat.ruleset.utils.leaveOnlyOneNewLine
import com.saveourtool.diktat.ruleset.utils.nextSibling
import com.saveourtool.diktat.ruleset.utils.prevSibling
import com.saveourtool.diktat.ruleset.utils.reversedChildren

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.psiUtil.startOffset

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

/**
 * Formatting visitor for Kdoc:
 * 1) removing all blank lines between Kdoc and the code it's declaring
 * 2) ensuring there are no tags with empty content
 * 3) ensuring there is only one white space between tag and it's body
 * 4) ensuring tags @apiNote, @implSpec, @implNote have one empty line after their body
 * 5) ensuring tags @param, @return, @throws are arranged in this order
 * 6) ensuring @author tag is not used
 * 7) ensuring @since tag contains only versions and not dates
 */
@Suppress("ForbiddenComment")
class KdocFormatting(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(KDOC_CONTAINS_DATE_OR_AUTHOR, KDOC_EMPTY_KDOC, KDOC_NEWLINES_BEFORE_BASIC_TAGS, KDOC_NO_DEPRECATED_TAG,
        KDOC_NO_EMPTY_TAGS, KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS, KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS,
        KDOC_WRONG_SPACES_AFTER_TAG, KDOC_WRONG_TAGS_ORDER),
) {
    /**
     * The reasoning behind the tag ordering:
     *
     * 1. `@receiver` documents `this` instance which is the 1st function call
     *    parameter (ordered before `@param`).
     * 1. `@param` followed by `@return`, then followed by `@throws` or
     *    `@exception` is the conventional order inherited from _JavaDoc_.
     * 1. `@param` tags can also be used to document generic type parameters.
     * 1. `@property` tags are placed after `@param` tags in the official
     *    [example](https://kotlinlang.org/docs/kotlin-doc.html#kdoc-syntax). Looking at the
     *    [code](https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/util/Tuples.kt#L22),
     *    this is also the style _JetBrains_ use themselves.
     * 1. looking at the examples, `@constructor` tags are placed last in
     *    constructor descriptions.
     */
    private val basicTagsList = listOf(
        KDocKnownTag.RECEIVER,
        KDocKnownTag.PARAM,
        KDocKnownTag.PROPERTY,
        KDocKnownTag.RETURN,
        KDocKnownTag.THROWS,
        KDocKnownTag.EXCEPTION,
        KDocKnownTag.CONSTRUCTOR,
    )
    private val specialTagNames = setOf("implSpec", "implNote", "apiNote")
    private var versionRegex: Regex? = null

    /**
     * @param node
     */
    override fun logic(node: ASTNode) {
        versionRegex ?: run {
            versionRegex = KdocFormatConfiguration(
                configRules.getRuleConfig(KDOC_CONTAINS_DATE_OR_AUTHOR)?.configuration ?: emptyMap()
            )
                .versionRegex
        }

        if (node.elementType == KDOC && isKdocNotEmpty(node)) {
            checkNoDeprecatedTag(node)
            checkEmptyTags(node.kDocTags())
            checkSpaceAfterTag(node.kDocTags())
            checkEmptyLineBeforeBasicTags(node.kDocBasicTags())
            checkEmptyLinesBetweenBasicTags(node.kDocBasicTags())
            checkBasicTagsOrder(node)
            checkNewLineAfterSpecialTags(node)
            checkAuthorAndDate(node)
        }
    }

    private fun isKdocNotEmpty(node: ASTNode): Boolean {
        val isKdocNotEmpty = node.getFirstChildWithType(KDocElementTypes.KDOC_SECTION)
            ?.hasChildMatching {
                it.elementType != KDocTokens.LEADING_ASTERISK && it.elementType != WHITE_SPACE
            } ?: false
        if (!isKdocNotEmpty) {
            KDOC_EMPTY_KDOC.warn(configRules, emitWarn,
                node.treeParent.getIdentifierName()?.text
                    ?: node.nextSibling { it.elementType in KtTokens.KEYWORDS }?.text
                    ?: node.text, node.startOffset, node)
        }
        return isKdocNotEmpty
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkNoDeprecatedTag(node: ASTNode) {
        val kdocTags = node.kDocTags()
        kdocTags.find { it.name == "deprecated" }
            ?.let { kdocTag ->
                KDOC_NO_DEPRECATED_TAG.warnAndFix(configRules, emitWarn, isFixMode, kdocTag.text, kdocTag.node.startOffset, kdocTag.node) {
                    val kdocSection = kdocTag.node.treeParent
                    val deprecatedTagNode = kdocTag.node
                    kdocSection.removeRange(deprecatedTagNode.prevSibling { it.elementType == WHITE_SPACE }!!,
                        deprecatedTagNode.nextSibling { it.elementType == WHITE_SPACE }
                    )
                    node.treeParent.addChild(LeafPsiElement(KtNodeTypes.ANNOTATION,
                        "@Deprecated(message = \"${kdocTag.getContent()}\")"), node.treeNext)
                    // copy to get all necessary indents
                    node.treeParent.addChild(node.nextSibling { it.elementType == WHITE_SPACE }!!.clone() as PsiWhiteSpaceImpl, node.treeNext)
                }
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkEmptyTags(kdocTags: Collection<KDocTag>?) {
        kdocTags?.filter {
            it.getSubjectName() == null && it.getContent().isEmpty()
        }?.forEach {
            KDOC_NO_EMPTY_TAGS.warn(configRules, emitWarn, "@${it.name!!}", it.node.startOffset, it.node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkSpaceAfterTag(kdocTags: Collection<KDocTag>?) {
        // tags can have 'parameters' and content, either can be missing
        // we always can find white space after tag name, but after tag parameters only if content is present
        kdocTags?.filter { tag ->
            val hasSubject = tag.getSubjectName()?.isNotBlank() ?: false
            if (!hasSubject && tag.getContent().isBlank()) {
                return@filter false
            }

            val (isSpaceBeforeContentError, isSpaceAfterTagError) = findBeforeAndAfterSpaces(tag)

            hasSubject && isSpaceBeforeContentError || isSpaceAfterTagError
        }?.forEach { tag ->
            KDOC_WRONG_SPACES_AFTER_TAG.warnAndFix(configRules, emitWarn, isFixMode,
                "@${tag.name!!}", tag.node.startOffset, tag.node) {
                tag.node.findChildBefore(KDocTokens.TEXT, WHITE_SPACE)
                    ?.let { tag.node.replaceChild(it, LeafPsiElement(WHITE_SPACE, " ")) }
                tag.node.findChildAfter(KDocTokens.TAG_NAME, WHITE_SPACE)
                    ?.let { tag.node.replaceChild(it, LeafPsiElement(WHITE_SPACE, " ")) }
            }
        }
    }

    private fun findBeforeAndAfterSpaces(tag: KDocTag) = Pair(tag.node.findChildBefore(KDocTokens.TEXT, WHITE_SPACE).let {
        it?.text != " " &&
                !(it?.isWhiteSpaceWithNewline() ?: false)
    },
        tag.node.findChildAfter(KDocTokens.TAG_NAME, WHITE_SPACE).let {
            it?.text != " " &&
                    !(it?.isWhiteSpaceWithNewline() ?: false)
        }
    )

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun checkBasicTagsOrder(node: ASTNode) {
        val kdocTags = node.kDocTags()
        // distinct basic tags which are present in current KDoc, in proper order
        val basicTagsOrdered = basicTagsList.filter { basicTag ->
            kdocTags.any { it.knownTag == basicTag }
        }
        // all basic tags from current KDoc
        val basicTags = kdocTags.filter { basicTagsOrdered.contains(it.knownTag) }
        val isTagsInCorrectOrder = basicTags
            .fold(mutableListOf<KDocTag>()) { acc, kdocTag ->
                if (acc.size > 0 && acc.last().knownTag != kdocTag.knownTag) {
                    acc.add(kdocTag)
                } else if (acc.size == 0) {
                    acc.add(kdocTag)
                }
                acc
            }
            .map { it.knownTag } == basicTagsOrdered

        if (!isTagsInCorrectOrder) {
            KDOC_WRONG_TAGS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                basicTags.joinToString(", ") { "@${it.name}" }, basicTags
                    .first()
                    .node
                    .startOffset, basicTags.first().node) {
                val basicTagChildren = kdocTags
                    .filter { basicTagsOrdered.contains(it.knownTag) }
                    .map { it.node }

                val correctKdocOrder = basicTags
                    .sortedBy { basicTagsOrdered.indexOf(it.knownTag) }
                    .map { it.node }

                basicTagChildren.mapIndexed { index, astNode ->
                    val kdocSection = astNode.treeParent
                    kdocSection.addChild(correctKdocOrder[index].clone() as CompositeElement, astNode)
                    kdocSection.removeChild(astNode)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun checkEmptyLineBeforeBasicTags(basicTags: List<KDocTag>) {
        val firstBasicTag = basicTags.firstOrNull() ?: return

        val hasContentBefore = firstBasicTag
            .node
            .allSiblings(true)
            .let { it.subList(0, it.indexOf(firstBasicTag.node)) }
            .any { it.elementType !in arrayOf(WHITE_SPACE, KDocTokens.LEADING_ASTERISK) && it.text.isNotBlank() }

        val previousTag = firstBasicTag.node.prevSibling { it.elementType == KDocElementTypes.KDOC_TAG }
        val hasEmptyLineBefore = previousTag?.hasEmptyLineAfter()
            ?: (firstBasicTag
                .node
                .previousAsterisk()
                ?.previousAsterisk()
                ?.treeNext
                ?.elementType == WHITE_SPACE)

        if (hasContentBefore xor hasEmptyLineBefore) {
            KDOC_NEWLINES_BEFORE_BASIC_TAGS.warnAndFix(configRules, emitWarn, isFixMode,
                "@${firstBasicTag.name!!}", firstBasicTag.node.startOffset, firstBasicTag.node) {
                if (hasContentBefore) {
                    previousTag?.applyToPrevSibling(KDocTokens.LEADING_ASTERISK) {
                        previousTag.addChild(treePrev.clone() as ASTNode, null)
                        previousTag.addChild(this.clone() as ASTNode, null)
                    }
                        ?: firstBasicTag.node.applyToPrevSibling(KDocTokens.LEADING_ASTERISK) {
                            treeParent.addChild(treePrev.clone() as ASTNode, this)
                            treeParent.addChild(this.clone() as ASTNode, treePrev)
                        }
                } else {
                    firstBasicTag.node.apply {
                        val asteriskNode = previousAsterisk()!!
                        treeParent.removeChild(asteriskNode.treePrev)
                        treeParent.removeChild(asteriskNode.treePrev)
                    }
                }
            }
        }
    }

    private fun checkEmptyLinesBetweenBasicTags(basicTags: List<KDocTag>) {
        val tagsWithRedundantEmptyLines = basicTags.dropLast(1).filter { tag ->
            val nextWhiteSpace = tag.node.nextSibling { it.elementType == WHITE_SPACE }
            // either there is a trailing blank line in tag's body OR there are empty lines right after this tag
            tag.hasTrailingNewlineInTagBody() || nextWhiteSpace?.text?.count { it == '\n' } != 1
        }

        tagsWithRedundantEmptyLines.forEach { tag ->
            KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnAndFix(configRules, emitWarn, isFixMode,
                "@${tag.name}", tag.startOffset, tag.node) {
                if (tag.hasTrailingNewlineInTagBody()) {
                    // if there is a blank line in tag's body, we remove it and everything after it, so that the next white space is kept in place
                    // we look for the last LEADING_ASTERISK and take its previous node which should be WHITE_SPACE with newline
                    tag.node.reversedChildren()
                        .takeWhile { it.elementType == WHITE_SPACE || it.elementType == KDocTokens.LEADING_ASTERISK }
                        .firstOrNull { it.elementType == KDocTokens.LEADING_ASTERISK }
                        ?.let { tag.node.removeRange(it.treePrev, null) }
                } else {
                    // otherwise we remove redundant blank lines from white space node after tag
                    tag.node.nextSibling { it.elementType == WHITE_SPACE }?.leaveOnlyOneNewLine()
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "ComplexMethod")
    private fun checkNewLineAfterSpecialTags(node: ASTNode) {
        val presentSpecialTagNodes = node
            .getFirstChildWithType(KDocElementTypes.KDOC_SECTION)
            ?.getAllChildrenWithType(KDocElementTypes.KDOC_TAG)
            ?.filter { (it.psi as KDocTag).name in specialTagNames }

        val poorlyFormattedTagNodes = presentSpecialTagNodes?.filterNot { specialTagNode ->
            // empty line with just * followed by white space or end of block
            specialTagNode.lastChildNode.elementType == KDocTokens.LEADING_ASTERISK &&
                    (specialTagNode.treeNext == null || specialTagNode.treeNext.elementType == WHITE_SPACE &&
                            specialTagNode.treeNext.text.count { it == '\n' } == 1) &&
                    // and with no empty line before
                    specialTagNode.lastChildNode.treePrev.elementType == WHITE_SPACE &&
                    specialTagNode.lastChildNode.treePrev.treePrev.elementType != KDocTokens.LEADING_ASTERISK
        }

        if (poorlyFormattedTagNodes != null && poorlyFormattedTagNodes.isNotEmpty()) {
            KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnAndFix(configRules, emitWarn, isFixMode,
                poorlyFormattedTagNodes.joinToString(", ") { "@${(it.psi as KDocTag).name!!}" },
                poorlyFormattedTagNodes.first().startOffset, node) {
                poorlyFormattedTagNodes.forEach { node ->
                    while (node.lastChildNode.elementType == KDocTokens.LEADING_ASTERISK && node.lastChildNode.treePrev.treePrev.elementType == KDocTokens.LEADING_ASTERISK) {
                        node.removeChild(node.lastChildNode)  // KDOC_LEADING_ASTERISK
                        node.removeChild(node.lastChildNode)  // WHITE_SPACE
                    }
                    if (node.treeParent.lastChildNode != node && node.lastChildNode.elementType != KDocTokens.LEADING_ASTERISK) {
                        val indent = node
                            .prevSibling { it.elementType == WHITE_SPACE }
                            ?.text
                            ?.substringAfter('\n')
                            ?.count { it == ' ' } ?: 0
                        node.addChild(PsiWhiteSpaceImpl("\n${" ".repeat(indent)}"), null)
                        node.addChild(LeafPsiElement(KDocTokens.LEADING_ASTERISK, "*"), null)
                    }
                }
            }
        }
    }

    private fun checkAuthorAndDate(node: ASTNode) {
        node.kDocTags()
            .filter {
                it.knownTag == KDocKnownTag.AUTHOR ||
                        it.knownTag == KDocKnownTag.SINCE && it.hasInvalidVersion()
            }
            .forEach {
                KDOC_CONTAINS_DATE_OR_AUTHOR.warn(configRules, emitWarn, it.text.trim(), it.startOffset, it.node)
            }
    }

    // fixme this method can be improved and extracted to utils
    private fun ASTNode.hasEmptyLineAfter(): Boolean {
        require(this.elementType == KDocElementTypes.KDOC_TAG) { "This check is only for KDOC_TAG" }
        return lastChildNode.elementType == KDocTokens.LEADING_ASTERISK &&
                (treeNext == null || treeNext.elementType == WHITE_SPACE && treeNext.text.count { it == '\n' } == 1)
    }

    private fun ASTNode.kDocBasicTags() = kDocTags().filter { basicTagsList.contains(it.knownTag) }

    private fun ASTNode.previousAsterisk() = prevSibling { it.elementType == KDocTokens.LEADING_ASTERISK }

    private fun ASTNode.applyToPrevSibling(elementType: IElementType, consumer: ASTNode.() -> Unit) {
        prevSibling { it.elementType == elementType }?.apply(consumer)
    }

    /**
     * Checks whether this tag's content represents an invalid version
     */
    private fun KDocTag.hasInvalidVersion(): Boolean {
        val content = getContent().trim()
        if (' ' in content || '/' in content) {
            // Filter based on symbols that are not allowed in versions. Assuming that people put either version or date in `@since` tag.
            return true
        }
        return versionRegex?.matches(content)?.not()
            ?: dateFormats.mapNotNull {
                // try to parse using some standard date patterns
                runCatching {
                    it.parse(content).get(ChronoField.YEAR)
                }
                    .getOrNull()
            }
                .isNotEmpty()
    }

    /**
     * A [RuleConfiguration] for KDoc formatting
     */
    class KdocFormatConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Regular expression, if present, against which a version should be matched in `@since` tag.
         */
        val versionRegex: Regex? by lazy {
            config["versionRegex"]?.let { Regex(it) }
        }
    }

    companion object {
        const val NAME_ID = "kdoc-formatting"
        val dateFormats: List<DateTimeFormatter> = listOf("yyyy-dd-mm", "yy-dd-mm", "yyyy-mm-dd", "yy-mm-dd", "yyyy.mm.dd", "yyyy.dd.mm")
            .map {
                DateTimeFormatter.ofPattern(it)
            }
    }
}
