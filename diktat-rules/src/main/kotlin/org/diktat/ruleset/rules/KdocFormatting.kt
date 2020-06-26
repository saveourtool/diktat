package org.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevSibling
import org.diktat.common.config.rules.RulesConfig
import org.diktat.ruleset.constants.Warnings.BLANK_LINE_AFTER_KDOC
import org.diktat.ruleset.constants.Warnings.KDOC_NO_DEPRECATED_TAG
import org.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS
import org.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS
import org.diktat.ruleset.constants.Warnings.KDOC_WRONG_SPACES_AFTER_TAG
import org.diktat.ruleset.constants.Warnings.KDOC_WRONG_TAGS_ORDER
import org.diktat.ruleset.utils.countSubStringOccurrences
import org.diktat.ruleset.utils.findChildAfter
import org.diktat.ruleset.utils.findChildBefore
import org.diktat.ruleset.utils.getAllChildrenWithType
import org.diktat.ruleset.utils.getFirstChildWithType
import org.diktat.ruleset.utils.kDocTags
import org.diktat.ruleset.utils.leaveOnlyOneNewLine
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Formatting visitor for Kdoc:
 * 1) removing all blank lines between Kdoc and the code it's declaring
 * 2) ensuring there are no tags with empty content
 * 3) ensuring there is only one white space between tag and it's body
 * 4) ensuring tags @apiNote, @implSpec, @implNote have one empty line after their body
 * 5) ensuring tags @param, @return, @throws are arranged in this order
 */
class KdocFormatting : Rule("kdoc-formatting") {
    private val basicTagsList = listOf(KDocKnownTag.PARAM, KDocKnownTag.RETURN, KDocKnownTag.THROWS)
    private val specialTagNames = setOf("implSpec", "implNote", "apiNote")

    private lateinit var confiRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        confiRules = params.rulesConfigList!!
        isFixMode = autoCorrect
        emitWarn = emit

        val declarationTypes = setOf(CLASS, FUN, PROPERTY)

        if (declarationTypes.contains(node.elementType)) {
            checkBlankLineAfterKdoc(node)
        }

        if (node.elementType == KDOC) {
            checkNoDeprecatedTag(node)
            checkEmptyTags(node.kDocTags())
            checkSpaceAfterTag(node.kDocTags())
            checkEmptyLinesAfterBasicTags(node)
            checkBasicTagsOrder(node)
            checkNewLineAfterSpecialTags(node)
        }
    }

    private fun checkBlankLineAfterKdoc(node: ASTNode) {
        val kdoc = node.getFirstChildWithType(KDOC)
        val nodeAfterKdoc = kdoc?.treeNext
        val name = node.getFirstChildWithType(ElementType.IDENTIFIER)
        if (nodeAfterKdoc?.elementType == WHITE_SPACE && nodeAfterKdoc.text.countSubStringOccurrences("\n") > 1) {
            BLANK_LINE_AFTER_KDOC.warnAndFix(confiRules, emitWarn, isFixMode, name!!.text, nodeAfterKdoc.startOffset) {
                nodeAfterKdoc.leaveOnlyOneNewLine()
            }
        }
    }

    private fun checkNoDeprecatedTag(node: ASTNode) {
        val kDocTags = node.kDocTags()
        kDocTags?.find { it.name == "deprecated" }
            ?.let { kDocTag ->
                KDOC_NO_DEPRECATED_TAG.warnAndFix(confiRules, emitWarn, isFixMode, kDocTag.text, kDocTag.node.startOffset) {
                    val kDocSection = kDocTag.node.treeParent
                    val deprecatedTagNode = kDocSection.getChildren(TokenSet.create(KDOC_TAG))
                        .find { "@deprecated" in it.text }!!
                    kDocSection.removeRange(deprecatedTagNode.prevSibling { it.elementType == WHITE_SPACE }!!,
                        deprecatedTagNode.nextSibling { it.elementType == WHITE_SPACE }
                    )
                    node.treeParent.addChild(LeafPsiElement(ElementType.ANNOTATION,
                        "@Deprecated(message = \"${kDocTag.getContent()}\")"), node.treeNext)
                    node.treeParent.addChild(node.nextSibling { it.elementType == WHITE_SPACE }!!.copyElement(), node.treeNext)  // copy to get all necessary indentatios
                }
            }
    }

    private fun checkEmptyTags(kDocTags: Collection<KDocTag>?) {
        kDocTags?.filter {
            it.getSubjectName() == null && it.getContent().isEmpty()
        }?.forEach {
            KDOC_NO_EMPTY_TAGS.warn(confiRules, emitWarn, isFixMode, "@${it.name!!}", it.node.startOffset)
        }
    }

    private fun checkSpaceAfterTag(kDocTags: Collection<KDocTag>?) {
        // tags can have 'parameters' and content, either can be missing
        // we always can find white space after tag name, but after tag parameters only if content is present
        kDocTags?.filter { tag ->
            tag.node.findChildBefore(KDOC_TEXT, WHITE_SPACE)?.let { it.text != " " } ?: false
                || tag.node.findChildAfter(KDOC_TAG_NAME, WHITE_SPACE)?.text != " "
        }?.forEach { tag ->
            KDOC_WRONG_SPACES_AFTER_TAG.warnAndFix(confiRules, emitWarn, isFixMode,
                "@${tag.name!!}", tag.node.startOffset) {
                tag.node.findChildBefore(KDOC_TEXT, WHITE_SPACE)
                    ?.let { tag.node.replaceChild(it, LeafPsiElement(WHITE_SPACE, " ")) }
                tag.node.findChildAfter(KDOC_TAG_NAME, WHITE_SPACE)
                    ?.let { tag.node.replaceChild(it, LeafPsiElement(WHITE_SPACE, " ")) }
            }
        }
    }

    private fun checkBasicTagsOrder(node: ASTNode) {
        val kDocTags = node.kDocTags()
        // distinct basic tags which are present in current KDoc, in proper order
        val basicTagsOrdered = basicTagsList.filter { basicTag ->
            kDocTags?.find { it.knownTag == basicTag } != null
        }
        // all basic tags from curent KDoc
        val basicTags = kDocTags?.filter { basicTagsOrdered.contains(it.knownTag) }
        val isTagsInCorrectOrder = basicTags
            ?.fold(mutableListOf<KDocTag>()) { acc, kDocTag ->
                if (acc.size > 0 && acc.last().knownTag != kDocTag.knownTag) acc.add(kDocTag)
                else if (acc.size == 0) acc.add(kDocTag)
                acc
            }
            ?.map { it.knownTag }?.equals(basicTagsOrdered)

        if (kDocTags != null && !isTagsInCorrectOrder!!) {
            KDOC_WRONG_TAGS_ORDER.warnAndFix(confiRules, emitWarn, isFixMode,
                basicTags.joinToString(", ") { "@${it.name}" }, basicTags.first().node.startOffset) {
                val kDocSection = node.getFirstChildWithType(KDOC_SECTION)!!
                val basicTagChildren = kDocTags
                    .filter { basicTagsOrdered.contains(it.knownTag) }
                    .map { it.node }

                basicTagsOrdered.forEachIndexed { index, tag ->
                    val tagNode = kDocTags.find { it.knownTag == tag }?.node
                    kDocSection.addChild(tagNode!!.copyElement(), basicTagChildren[index])
                    kDocSection.removeChild(basicTagChildren[index])
                }
            }
        }
    }

    private fun checkEmptyLinesAfterBasicTags(node: ASTNode) {
        val kDocTags = node.kDocTags()
        val basicTags = kDocTags?.filter { basicTagsList.contains(it.knownTag) }

        val tagsWithRedundantEmptyLines = basicTags?.dropLast(1)?.filterNot { tag ->
            val nextWhiteSpace = tag.node.nextSibling { it.elementType == WHITE_SPACE }
            val noEmptyKdocLines = tag.node.getChildren(TokenSet.create(KDOC_LEADING_ASTERISK))
                .filter { it.treeNext == null || it.treeNext.elementType == WHITE_SPACE }
                .count() == 0
            nextWhiteSpace?.text?.count { it == '\n' } == 1 && noEmptyKdocLines
        }

        tagsWithRedundantEmptyLines?.forEach { tag ->
            KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnAndFix(confiRules, emitWarn, isFixMode,
                "@${tag.name}", tag.startOffset) {
                tag.node.nextSibling { it.elementType == WHITE_SPACE }?.leaveOnlyOneNewLine()
                // the first asterisk before tag is not included inside KDOC_TAG node
                // we look for the second and take its previous which should be WHITE_SPACE with newline
                tag.node.getAllChildrenWithType(KDOC_LEADING_ASTERISK).firstOrNull()
                    ?.let { tag.node.removeRange(it.treePrev, null) }
            }
        }
    }

    private fun checkNewLineAfterSpecialTags(node: ASTNode) {
        val presentSpecialTagNodes = node.getFirstChildWithType(KDOC_SECTION)
            ?.getAllChildrenWithType(KDOC_TAG)
            ?.filter { (it.psi as KDocTag).name in specialTagNames }

        val poorlyFormattedTagNodes = presentSpecialTagNodes?.filterNot { specialTagNode ->
            // empty line with just * followed by white space or end of block
            specialTagNode.lastChildNode.elementType == KDOC_LEADING_ASTERISK
                && (specialTagNode.treeNext == null || specialTagNode.treeNext.elementType == WHITE_SPACE
                && specialTagNode.treeNext.text.count { it == '\n' } == 1)
                // and with no empty line before
                && specialTagNode.lastChildNode.treePrev.elementType == WHITE_SPACE
                && specialTagNode.lastChildNode.treePrev.treePrev.elementType != KDOC_LEADING_ASTERISK
        }

        if (presentSpecialTagNodes != null && poorlyFormattedTagNodes!!.isNotEmpty()) {
            KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnAndFix(confiRules, emitWarn, isFixMode,
                poorlyFormattedTagNodes.joinToString(", ") { "@${(it.psi as KDocTag).name!!}" },
                poorlyFormattedTagNodes.first().startOffset) {
                poorlyFormattedTagNodes.forEach { node ->
                    while (node.lastChildNode.elementType == KDOC_LEADING_ASTERISK
                        && node.lastChildNode.treePrev.treePrev.elementType == KDOC_LEADING_ASTERISK) {
                        node.removeChild(node.lastChildNode)  // KDOC_LEADING_ASTERISK
                        node.removeChild(node.lastChildNode)  // WHITE_SPACE
                    }
                    if (node.lastChildNode.elementType != KDOC_LEADING_ASTERISK) {
                        val indent = node.prevSibling { it.elementType == WHITE_SPACE }
                            ?.text?.substringAfter('\n')?.count { it == ' ' } ?: 0
                        node.addChild(LeafPsiElement(WHITE_SPACE, "\n${" ".repeat(indent)}"), null)
                        node.addChild(LeafPsiElement(KDOC_LEADING_ASTERISK, "*"), null)
                    }
                }
            }
        }
    }
}
