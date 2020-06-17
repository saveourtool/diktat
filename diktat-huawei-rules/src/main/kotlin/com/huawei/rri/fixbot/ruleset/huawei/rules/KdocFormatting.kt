package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.*
import com.huawei.rri.fixbot.ruleset.huawei.utils.*
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

/**
 * Formatting visitor for Kdoc:
 * 1) removing all blank lines between Kdoc and the code it's declaring
 * 2) ensuring there are no tags with empty content
 * 3) ensuring there is only one white space between tag and it's body
 * 4) ensuring tags @apiNote, @implSpec, @implNote have one empty line after their body
 * 5) ensuring tags @param, @return, @throws are arranged in this order
 */
class KdocFormatting : Rule("kdoc-formatting") {

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
            val kDocTags = node.kDocTags()

            checkNoDeprecatedTag(kDocTags)
            checkEmptyTags(kDocTags)
            checkSpaceAfterTag(kDocTags)
            checkBasicTagsOrder(node, kDocTags)
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

    private fun checkNoDeprecatedTag(kDocTags: Collection<KDocTag>?) {
        kDocTags?.find { it.name == "deprecated" }
            ?.let {
                // fixme possible fix would be to remove tag and add annotation, but description should be kept
                //  also annotation supports different fields, which ideally should be properly filled
                KDOC_NO_DEPRECATED_TAG.warn(confiRules, emitWarn, isFixMode, it.text, it.node.startOffset)
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
        kDocTags?.filter {
            it.node.findChildAfter(KDOC_TAG_NAME, WHITE_SPACE)?.text != " "
        }?.forEach {
            KDOC_WRONG_SPACES_AFTER_TAG.warnAndFix(confiRules, emitWarn, isFixMode,
                "@${it.name!!}", it.node.startOffset) {
                val whitespaceNode = it.node.findChildAfter(KDOC_TAG_NAME, WHITE_SPACE)
                if (whitespaceNode != null) {
                    it.node.replaceChild(whitespaceNode, LeafPsiElement(WHITE_SPACE, " "))
                }
            }
        }
    }

    private fun checkBasicTagsOrder(node: ASTNode, kDocTags: Collection<KDocTag>?) {
        // basic tags which are present in current KDoc, in proper order
        val basicTagsOrdered = listOf(KDocKnownTag.PARAM, KDocKnownTag.RETURN, KDocKnownTag.THROWS)
            .filter { basicTag -> kDocTags?.find { it.knownTag == basicTag } != null }
        val basicTags = kDocTags?.filter { basicTagsOrdered.contains(it.knownTag) }
        val isTagsInCorrectOrder = basicTags?.map { it.knownTag }?.equals(basicTagsOrdered)

        if (kDocTags != null && !isTagsInCorrectOrder!!) {
            KDOC_WRONG_TAGS_ORDER.warnAndFix(confiRules, emitWarn, isFixMode,
                basicTags.joinToString(", ") { "@${it.name}" }, basicTags.first().node.startOffset) {
                val kDocSection = node.getFirstChildWithType(ElementType.KDOC_SECTION)!!
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

    private fun checkNewLineAfterSpecialTags(node: ASTNode) {
        val specialTagNames = listOf("implSpec", "implNote", "apiNote")

        val presentSpecialTagNodes = node.getFirstChildWithType(ElementType.KDOC_SECTION)
            ?.getAllChildrenWithType(ElementType.KDOC_TAG)
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
