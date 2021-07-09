package org.cqfn.diktat.ruleset.rules.chapter2.kdoc

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMMENT_WHITE_SPACE
import org.cqfn.diktat.ruleset.constants.Warnings.FIRST_COMMENT_NO_BLANK_LINE
import org.cqfn.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NEWLINES_AROUND_KDOC
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_CODE_BLOCK_TEXT
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * This class handles rule 2.6
 * Part 1:
 * * there must be 1 space between the comment character and the content of the comment;
 * * there must be a newline between a Kdoc and the previous code above and no blank lines after.
 * * No need to add a blank line before a first comment in this particular name space (code block), for example between function declaration
 *   and first comment in a function body.
 *
 * Part 2:
 * * Leave one single space between the comment on the right side of the code and the code.
 * * Comments in if else should be inside code blocks. Exception: General if comment
 */
class CommentsFormatting(configRules: List<RulesConfig>) : DiktatRule(
    "kdoc-comments-codeblocks-formatting",
    configRules,
    listOf(COMMENT_WHITE_SPACE, FIRST_COMMENT_NO_BLANK_LINE,
        IF_ELSE_COMMENTS, WRONG_NEWLINES_AROUND_KDOC)) {
    /**
     * @param node
     */
    override fun logic(node: ASTNode) {
        val configuration = CommentsFormattingConfiguration(
            configRules.getRuleConfig(COMMENT_WHITE_SPACE)?.configuration ?: emptyMap())

        when (node.elementType) {
            CLASS, FUN, PROPERTY -> checkBlankLineAfterKdoc(node)
            IF -> handleIfElse(node)
            EOL_COMMENT, BLOCK_COMMENT -> handleEolAndBlockComments(node, configuration)
            KDOC -> handleKdocComments(node, configuration)
            else -> {
                // this is a generated else block
            }
        }
    }

    private fun checkBlankLineAfterKdoc(node: ASTNode) {
        commentType.forEach {
            val kdoc = node.getFirstChildWithType(it)
            val nodeAfterKdoc = kdoc?.treeNext
            if (nodeAfterKdoc?.elementType == WHITE_SPACE && nodeAfterKdoc.numNewLines() > 1) {
                WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, "redundant blank line after ${kdoc.text}", nodeAfterKdoc.startOffset, nodeAfterKdoc) {
                    nodeAfterKdoc.leaveOnlyOneNewLine()
                }
            }
        }
    }

    private fun handleKdocComments(node: ASTNode, configuration: CommentsFormattingConfiguration) {
        if (node.treeParent.treeParent != null && node.treeParent.treeParent.elementType == BLOCK) {
            checkCommentsInCodeBlocks(node.treeParent)  // node.treeParent is a node that contains a comment.
        } else if (node.treeParent.elementType != IF) {
            checkClassComment(node)
        }
        checkWhiteSpaceBeginInComment(node, configuration)
    }

    private fun handleEolAndBlockComments(node: ASTNode, configuration: CommentsFormattingConfiguration) {
        basicCommentsChecks(node, configuration)
        checkWhiteSpaceBeginInComment(node, configuration)
    }

    private fun basicCommentsChecks(node: ASTNode, configuration: CommentsFormattingConfiguration) {
        checkSpaceBeforeComment(node, configuration)

        if (node.treeParent.elementType == BLOCK && node.treeNext != null) {
            // Checking if comment is inside a code block like fun{}
            // Not checking last comment as well
            if (isFirstComment(node)) {
                if (node.isBlockOrClassBody()) {
                    // Just check white spaces before comment
                    checkFirstCommentSpaces(node)
                }
                return
            }
        } else if (node.treeParent.lastChildNode != node && node.treeParent.elementType != IF &&
                node.treeParent.firstChildNode == node && node.treeParent.elementType != VALUE_ARGUMENT_LIST) {
            // Else it's a class comment
            checkClassComment(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleIfElse(node: ASTNode) {
        if (node.hasChildOfType(ELSE)) {
            val elseKeyWord = node.getFirstChildWithType(ELSE_KEYWORD)!!
            val elseBlock = node.getFirstChildWithType(ELSE)!!
            val comment = when {
                elseKeyWord.prevNodeUntilNode(THEN, EOL_COMMENT) != null -> elseKeyWord.prevNodeUntilNode(THEN, EOL_COMMENT)
                elseKeyWord.prevNodeUntilNode(THEN, BLOCK_COMMENT) != null -> elseKeyWord.prevNodeUntilNode(THEN, BLOCK_COMMENT)
                elseKeyWord.prevNodeUntilNode(THEN, KDOC) != null -> elseKeyWord.prevNodeUntilNode(THEN, KDOC)
                else -> null
            }
            val copyComment = comment?.copyElement()
            comment?.let {
                IF_ELSE_COMMENTS.warnAndFix(configRules, emitWarn, isFixMode, it.text, node.startOffset, node) {
                    moveCommentToElse(node, elseBlock, elseKeyWord, it, copyComment)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun moveCommentToElse(node: ASTNode,
                                  elseBlock: ASTNode,
                                  elseKeyWord: ASTNode,
                                  comment: ASTNode,
                                  copyComment: ASTNode?) {
        if (elseBlock.hasChildOfType(BLOCK)) {
            val elseCodeBlock = elseBlock.getFirstChildWithType(BLOCK)!!
            elseCodeBlock.addChild(copyComment!!,
                elseCodeBlock.firstChildNode.treeNext)
            elseCodeBlock.addChild(PsiWhiteSpaceImpl("\n"),
                elseCodeBlock.firstChildNode.treeNext)
            node.removeChild(comment)
        } else {
            elseKeyWord.treeParent.addChild(copyComment!!, elseKeyWord.treeNext)
            elseKeyWord.treeParent.addChild(PsiWhiteSpaceImpl("\n"), elseKeyWord.treeNext)
            node.removeChild(comment)
        }

        val whiteSpace = elseKeyWord.prevNodeUntilNode(THEN, WHITE_SPACE)

        whiteSpace?.let {
            node.removeChild(it)
        }
    }

    private fun checkCommentsInCodeBlocks(node: ASTNode) {
        if (isFirstComment(node)) {
            if (node.isBlockOrClassBody()) {
                // Just check white spaces before comment
                checkFirstCommentSpaces(node)
            }
            return
        }

        if (!node.treePrev.isWhiteSpace()) {
            // If node treePrev is not a whiteSpace then node treeParent is a property
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent)
            }
        } else {
            if (node.treePrev.numNewLines() == 1 || node.treePrev.numNewLines() > 2) {
                WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                    (node.treePrev as LeafPsiElement).rawReplaceWithText("\n\n")
                }
            }
        }
    }

    private fun checkSpaceBeforeComment(node: ASTNode, configuration: CommentsFormattingConfiguration) {
        if (node.treeParent.firstChildNode == node) {
            return
        }
        if (node.treeParent.elementType == FILE) {
            // This case is for top-level comments that are located in the beginning of the line and therefore don't need any spaces before.
            if (!node.treePrev.isWhiteSpaceWithNewline() && node.treePrev.text.count { it == ' ' } > 0) {
                COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode,
                    "There should be 0 space(s) before comment text, but are ${node.treePrev.text.count { it == ' ' }} in ${node.text}",
                    node.startOffset, node) {
                    if (node.treePrev.elementType == WHITE_SPACE) {
                        (node.treePrev as LeafPsiElement).rawReplaceWithText("\n")
                    } else {
                        node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node)
                    }
                }
            }
        } else if (!node.treePrev.isWhiteSpace()) {
            // if comment is like this: val a = 5// Comment
            COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode,
                "There should be ${configuration.maxSpacesBeforeComment} space(s) before comment text, but are none in ${node.text}", node.startOffset, node) {
                node.treeParent.addChild(PsiWhiteSpaceImpl(" ".repeat(configuration.maxSpacesBeforeComment)), node)
            }
        } else if (!node.treePrev.textContains('\n') && node.treePrev.text.length != configuration.maxSpacesBeforeComment) {
            // if there are too many spaces before comment
            COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode,
                "There should be ${configuration.maxSpacesBeforeComment} space(s) before comment text, but there are too many in ${node.text}", node.startOffset, node) {
                (node.treePrev as LeafPsiElement).rawReplaceWithText(" ".repeat(configuration.maxSpacesBeforeComment))
            }
        }
    }

    @Suppress("ComplexMethod", "TOO_LONG_FUNCTION")
    private fun checkWhiteSpaceBeginInComment(node: ASTNode, configuration: CommentsFormattingConfiguration) {
        if (node.elementType == EOL_COMMENT &&
                node
                    .text
                    .trimStart('/')
                    .takeWhile { it == ' ' }
                    .length == configuration.maxSpacesInComment) {
            return
        }

        if (node.elementType == BLOCK_COMMENT &&
                (node
                    .text
                    .trim('/', '*')
                    .takeWhile { it == ' ' }
                    .length == configuration.maxSpacesInComment ||
                        node
                            .text
                            .trim('/', '*')
                            .takeWhile { it == '\n' }
                            .isNotEmpty())) {
            return
        }

        if (node.elementType == KDOC) {
            val section = node.getFirstChildWithType(KDOC_SECTION)
            if (section != null &&
                    section.findChildrenMatching(KDOC_TEXT) { (it.treePrev != null && it.treePrev.elementType == KDOC_LEADING_ASTERISK) || it.treePrev == null }
                        .all { it.text.startsWith(" ".repeat(configuration.maxSpacesInComment)) }) {
                // it.treePrev == null if there is no \n at the beginning of KDoc
                return
            }

            if (section != null &&
                    section.getAllChildrenWithType(KDOC_CODE_BLOCK_TEXT).isNotEmpty() &&
                    section.getAllChildrenWithType(KDOC_CODE_BLOCK_TEXT).all { it.text.startsWith(" ".repeat(configuration.maxSpacesInComment)) }) {
                return
            }
        }

        COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode,
            "There should be ${configuration.maxSpacesInComment} space(s) before comment token in ${node.text}", node.startOffset, node) {
            val commentText = node.text.drop(2).trim()

            when (node.elementType) {
                EOL_COMMENT -> (node as LeafPsiElement).rawReplaceWithText("// $commentText")
                BLOCK_COMMENT -> (node as LeafPsiElement).rawReplaceWithText("/* $commentText")
                KDOC -> {
                    node.findAllDescendantsWithSpecificType(KDOC_TEXT).forEach {
                        modifyKdocText(it, configuration)
                    }
                    node.findAllDescendantsWithSpecificType(KDOC_CODE_BLOCK_TEXT).forEach {
                        modifyKdocText(it, configuration)
                    }
                }
            }
        }
    }

    private fun modifyKdocText(node: ASTNode, configuration: CommentsFormattingConfiguration) {
        if (!node.text.startsWith(" ".repeat(configuration.maxSpacesInComment))) {
            val commentText = node.text.trim()
            val indent = " ".repeat(configuration.maxSpacesInComment)
            (node as LeafPsiElement).rawReplaceWithText("$indent$commentText")
        }
    }

    private fun checkClassComment(node: ASTNode) {
        if (isFirstComment(node)) {
            if (node.isBlockOrClassBody()) {
                checkFirstCommentSpaces(node)
            } else {
                checkFirstCommentSpaces(node.treeParent)
            }
        } else if (node.treeParent.elementType != FILE && !node.treeParent.treePrev.isWhiteSpace()) {
            // fixme: we might face more issues because newline node is inserted in a wrong place which causes consecutive
            // white spaces to be split among nodes on different levels. But this requires investigation.
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                node.treeParent.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent)
            }
        } else if (node.treeParent.elementType != FILE &&
                (node.treeParent.treePrev.numNewLines() == 1 || node.treeParent.treePrev.numNewLines() > 2)) {
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                (node.treeParent.treePrev as LeafPsiElement).rawReplaceWithText("\n\n")
            }
        }
    }

    private fun checkFirstCommentSpaces(node: ASTNode) {
        if (node.treePrev.isWhiteSpace() &&
                (node.treePrev.numNewLines() > 1 || node.treePrev.numNewLines() == 0)) {
            FIRST_COMMENT_NO_BLANK_LINE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                (node.treePrev as LeafPsiElement).rawReplaceWithText("\n")
            }
        }
    }

    private fun isFirstComment(node: ASTNode) = if (node.isBlockOrClassBody()) {
        // In case when comment is inside of a function or class
        if (node.treePrev.isWhiteSpace()) {
            node.treePrev.treePrev.elementType == LBRACE
        } else {
            node.treePrev == null || node.treePrev.elementType == LBRACE  // null is handled for functional literal
        }
    } else if (node.treeParent?.treeParent?.elementType == FILE && node.treeParent.prevSibling { it.text.isNotBlank() } == null) {
        // `treeParent` is the first not-empty node in a file
        true
    } else if (node.treeParent.elementType != FILE && node.treeParent.treePrev != null &&
            node.treeParent.treePrev.treePrev != null) {
        // When comment inside of a PROPERTY
        node.treeParent.treePrev.treePrev.elementType == LBRACE
    } else {
        node.treeParent.getAllChildrenWithType(node.elementType).first() == node
    }

    private fun ASTNode.isBlockOrClassBody(): Boolean = treeParent.elementType == BLOCK || treeParent.elementType == CLASS_BODY

    /**
     * [RuleConfiguration] for [CommentsFormatting] rule
     */
    class CommentsFormattingConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Number of spaces before comment start
         */
        val maxSpacesBeforeComment = config["maxSpacesBeforeComment"]?.toIntOrNull() ?: MAX_SPACES

        /**
         * Number of spaces after comment sign (`//` or other)
         */
        val maxSpacesInComment = config["maxSpacesInComment"]?.toIntOrNull() ?: APPROPRIATE_COMMENT_SPACES
    }
    companion object {
        private const val APPROPRIATE_COMMENT_SPACES = 1
        private const val MAX_SPACES = 1
    }
}
