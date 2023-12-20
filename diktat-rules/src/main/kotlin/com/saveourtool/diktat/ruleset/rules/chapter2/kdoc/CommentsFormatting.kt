package com.saveourtool.diktat.ruleset.rules.chapter2.kdoc

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.COMMENT_WHITE_SPACE
import com.saveourtool.diktat.ruleset.constants.Warnings.FIRST_COMMENT_NO_BLANK_LINE
import com.saveourtool.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_NEWLINES_AROUND_KDOC
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.commentType
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.findChildrenMatching
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline
import com.saveourtool.diktat.ruleset.utils.leaveOnlyOneNewLine
import com.saveourtool.diktat.ruleset.utils.numNewLines
import com.saveourtool.diktat.ruleset.utils.prevNodeUntilNode
import com.saveourtool.diktat.ruleset.utils.prevSibling
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.ELSE
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.IF
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.ELSE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

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
    NAME_ID,
    configRules,
    listOf(COMMENT_WHITE_SPACE, FIRST_COMMENT_NO_BLANK_LINE,
        IF_ELSE_COMMENTS, WRONG_NEWLINES_AROUND_KDOC),
) {
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

    @Suppress("PARAMETER_NAME_IN_OUTER_LAMBDA")
    private fun checkBlankLineAfterKdoc(node: ASTNode) {
        commentType.forEach {
            val kdoc = node.getFirstChildWithType(it)
            kdoc?.treeNext?.let { nodeAfterKdoc ->
                if (nodeAfterKdoc.elementType == WHITE_SPACE && nodeAfterKdoc.numNewLines() > 1) {
                    WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, "redundant blank line after ${kdoc.text}", nodeAfterKdoc.startOffset, nodeAfterKdoc) {
                        nodeAfterKdoc.leaveOnlyOneNewLine()
                    }
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
                if (node.isChildOfBlockOrClassBody()) {
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
            comment?.let {
                IF_ELSE_COMMENTS.warnAndFix(configRules, emitWarn, isFixMode, comment.text, node.startOffset, node) {
                    moveCommentToElse(node, elseBlock, elseKeyWord, comment)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun moveCommentToElse(node: ASTNode,
                                  elseBlock: ASTNode,
                                  elseKeyWord: ASTNode,
                                  comment: ASTNode,
    ) {
        if (elseBlock.hasChildOfType(BLOCK)) {
            val elseCodeBlock = elseBlock.getFirstChildWithType(BLOCK)!!
            elseCodeBlock.addChild(comment,
                elseCodeBlock.firstChildNode.treeNext)
            elseCodeBlock.addChild(ASTFactory.whitespace("\n"),
                elseCodeBlock.firstChildNode.treeNext)
        } else {
            elseKeyWord.treeParent.addChild(comment, elseKeyWord.treeNext)
            elseKeyWord.treeParent.addChild(ASTFactory.whitespace("\n"), elseKeyWord.treeNext)
        }

        val whiteSpace = elseKeyWord.prevNodeUntilNode(THEN, WHITE_SPACE)

        whiteSpace?.let {
            node.removeChild(it)
        }
    }

    private fun checkCommentsInCodeBlocks(node: ASTNode) {
        if (isFirstComment(node)) {
            if (node.isChildOfBlockOrClassBody()) {
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
        if (node.treeParent.elementType == KtFileElementType.INSTANCE) {
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
            // if there are too many or too few spaces before comment
            val manyOrFew = when {
                node.treePrev.text.length > configuration.maxSpacesBeforeComment -> "many"
                else -> "few"
            }
            val message = "There should be ${configuration.maxSpacesBeforeComment} space(s) before comment text, but there are too $manyOrFew in ${node.text}"
            COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode,
                message, node.startOffset, node) {
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
                (node.isIndentStyleComment() ||
                        node
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
            val section = node.getFirstChildWithType(KDocElementTypes.KDOC_SECTION)
            if (section != null &&
                    section.findChildrenMatching(KDocTokens.TEXT) { (it.treePrev != null && it.treePrev.elementType == KDocTokens.LEADING_ASTERISK) || it.treePrev == null }
                        .all { it.text.startsWith(" ".repeat(configuration.maxSpacesInComment)) }) {
                // it.treePrev == null if there is no \n at the beginning of KDoc
                return
            }

            if (section != null &&
                    section.getAllChildrenWithType(KDocTokens.CODE_BLOCK_TEXT).isNotEmpty() &&
                    section.getAllChildrenWithType(KDocTokens.CODE_BLOCK_TEXT).all { it.text.startsWith(" ".repeat(configuration.maxSpacesInComment)) }) {
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
                    node.findAllDescendantsWithSpecificType(KDocTokens.TEXT).forEach {
                        modifyKdocText(it, configuration)
                    }
                    node.findAllDescendantsWithSpecificType(KDocTokens.CODE_BLOCK_TEXT).forEach {
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
            if (node.isChildOfBlockOrClassBody()) {
                checkFirstCommentSpaces(node)
            } else {
                checkFirstCommentSpaces(node.treeParent)
            }
        } else if (node.treeParent.elementType != KtFileElementType.INSTANCE && !node.treeParent.treePrev.isWhiteSpace()) {
            // fixme: we might face more issues because newline node is inserted in a wrong place which causes consecutive
            // white spaces to be split among nodes on different levels. But this requires investigation.
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                node.treeParent.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent)
            }
        } else if (node.treeParent.elementType != KtFileElementType.INSTANCE &&
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

    private fun isFirstComment(node: ASTNode) = if (node.isChildOfBlockOrClassBody()) {
        // In case when comment is inside of a function or class
        if (node.treePrev.isWhiteSpace()) {
            // in some cases (e.g. kts files) BLOCK doesn't have a leading brace
            node.treePrev?.treePrev?.elementType == LBRACE
        } else {
            node.treePrev == null || node.treePrev.elementType == LBRACE  // null is handled for functional literal
        }
    } else if (node.treeParent?.treeParent?.elementType == KtFileElementType.INSTANCE && node.treeParent.prevSibling { it.text.isNotBlank() } == null) {
        // `treeParent` is the first not-empty node in a file
        true
    } else if (node.treeParent.elementType != KtFileElementType.INSTANCE && node.treeParent.treePrev != null &&
            node.treeParent.treePrev.treePrev != null) {
        // When comment inside of a PROPERTY
        node.treeParent
            .treePrev
            .treePrev
            .elementType == LBRACE
    } else {
        node.treeParent.getAllChildrenWithType(node.elementType).first() == node
    }

    private fun ASTNode.isChildOfBlockOrClassBody(): Boolean = treeParent.elementType == BLOCK || treeParent.elementType == CLASS_BODY

    /**
     * Returns whether this block comment is a `indent`-style comment.
     *
     * `indent(1)` is a source code formatting utility for C-like languages.
     * Historically, source code formatters are permitted to reformat and reflow
     * the content of block comments, except for those comments which start with
     * "&#x2f;*-".
     *
     * See also:
     * - [5.1.1 Block Comments](https://www.oracle.com/java/technologies/javase/codeconventions-comments.html)
     * - [`indent(1)`](https://man.openbsd.org/indent.1)
     * - [`style(9)`](https://www.freebsd.org/cgi/man.cgi?query=style&sektion=9)
     *
     * @return `true` if this block comment is a `indent`-style comment, `false`
     *   otherwise.
     */
    private fun ASTNode.isIndentStyleComment(): Boolean {
        require(elementType == BLOCK_COMMENT) {
            "The elementType of this node is $elementType while $BLOCK_COMMENT expected"
        }

        return text.matches(indentCommentMarker)
    }

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
        const val NAME_ID = "kdoc-comments-codeblocks-formatting"

        /**
         * "&#x2f;*-" followed by anything but `*` or `-`.
         */
        private val indentCommentMarker = Regex("""(?s)^\Q/*-\E[^*-].*?""")
    }
}
