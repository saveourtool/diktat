package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
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
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NEWLINES_AROUND_KDOC
import org.cqfn.diktat.ruleset.constants.Warnings.COMMENT_WHITE_SPACE
import org.cqfn.diktat.ruleset.constants.Warnings.FIRST_COMMENT_NO_SPACES
import org.cqfn.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.countSubStringOccurrences
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.leaveOnlyOneNewLine
import org.cqfn.diktat.ruleset.utils.numNewLines
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType


/**
 * This class handles rule 2.6
 * Part 1:
 * there must be 1 space between the comment character and the content of the comment;
 * there must be a newline between a Kdoc and the previous code above.
 * No need to add a blank line before a first comment in this particular name space (code block), for example between function declaration and first comment in a function body.
 *
 * Part 2:
 * Leave one single space between the comment on the right side of the code and the code.
 * comments in if-else
 */
class CommentsFormatting(private val configRules: List<RulesConfig>) : Rule("kdoc-comments-codeblocks-formatting") {

    companion object {
        private const val MAX_SPACES = 1
        private const val APPROPRIATE_COMMENT_SPACES = 1
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        val configuration = KdocCodeBlocksFormattingConfiguration(
                configRules.getRuleConfig(COMMENT_WHITE_SPACE)?.configuration ?: mapOf())


        when (node.elementType) {
            CLASS, FUN, PROPERTY -> {
                checkBlankLineAfterKdoc(node, EOL_COMMENT)
                checkBlankLineAfterKdoc(node, KDOC)
                checkBlankLineAfterKdoc(node, BLOCK_COMMENT)
            }
            IF -> {
                handleIfElse(node)
            }
            EOL_COMMENT -> {
                handleEolComments(node, configuration)
            }
            BLOCK_COMMENT -> {
                handleBlockComments(node, configuration)
            }
            KDOC -> {
                handleKdocComments(node)
            }
        }
    }

    private fun checkBlankLineAfterKdoc(node: ASTNode, type: IElementType) {
        val kdoc = node.getFirstChildWithType(type)
        val nodeAfterKdoc = kdoc?.treeNext
        if (nodeAfterKdoc?.elementType == ElementType.WHITE_SPACE && nodeAfterKdoc.numNewLines() > 1) {
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, kdoc.text, nodeAfterKdoc.startOffset) {
                nodeAfterKdoc.leaveOnlyOneNewLine()
            }
        }
    }

    private fun handleKdocComments(node: ASTNode) {
        if (node.treeParent.treeParent.elementType == BLOCK) {
            checkCommentsInCodeBlocks(node.treeParent) // node.treeParent is a node that contains a comment.
        } else if (node.treeParent.elementType != IF){
            checkClassComment(node)
        }
    }

    private fun handleEolComments(node: ASTNode, configuration: KdocCodeBlocksFormattingConfiguration) {
        checkSpaceBetweenPropertyAndComment(node, configuration)

        if (node.treeParent.elementType == BLOCK && node.treeNext != null) {
            checkCommentsInCodeBlocks(node)
        } else if (node.treeParent.lastChildNode != node && node.treeParent.elementType != IF) {
            checkClassComment(node)
        }

        checkWhiteSpaceBeforeComment(node)
    }

    private fun handleBlockComments(node: ASTNode, configuration: KdocCodeBlocksFormattingConfiguration) {
        checkSpaceBetweenPropertyAndComment(node, configuration)

        if (node.treeParent.elementType == BLOCK && node.treeNext != null) {
            checkCommentsInCodeBlocks(node)
        } else if (node.treeParent.lastChildNode != node && node.treeParent.elementType != IF) {
            checkClassComment(node)
        }
    }

    private fun handleIfElse(node: ASTNode) {
        if(node.hasChildOfType(ELSE)) {
            val elseKeyWord = node.getFirstChildWithType(ELSE_KEYWORD)!!
            if(elseKeyWord.treePrev.treePrev.elementType.let { it == EOL_COMMENT ||
                            it == KDOC || it == BLOCK_COMMENT }) {
                IF_ELSE_COMMENTS.warnAndFix(configRules, emitWarn, isFixMode, elseKeyWord.treePrev.treePrev.text, node.startOffset) {
                    val elseBlock = node.getFirstChildWithType(ELSE)!!
                    if(elseBlock.hasChildOfType(BLOCK)) {
                        elseBlock.getFirstChildWithType(BLOCK)!!.addChild(elseKeyWord.treePrev.treePrev,
                                elseBlock.getFirstChildWithType(BLOCK)!!.firstChildNode.treeNext)
                        elseBlock.getFirstChildWithType(BLOCK)!!.addChild(PsiWhiteSpaceImpl("\n"),
                                elseBlock.getFirstChildWithType(BLOCK)!!.firstChildNode.treeNext)
                        node.removeChild(elseKeyWord.treePrev.treePrev)
                    } else {
                        val text = "else { \n${elseBlock.treePrev.treePrev.text}\n ${elseBlock.text} \n }"
                        node.removeChild(elseBlock)
                        node.addChild(KotlinParser().createNode(text), null)
                    }

                }
            }
        }
    }

    private fun checkCommentsInCodeBlocks(node: ASTNode) {
        if (isFirstComment(node)) {
            if (node.treeParent.elementType == BLOCK || node.treeParent.elementType == CLASS_BODY)
                checkFirstCommentSpaces(node)
            else
                checkFirstCommentSpaces(node.treeParent) // TreeParent is property. Then check white spaces before property
            return
        }

        if (!node.treePrev.isWhiteSpace()) {
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent) // If treeParent is property
            }
        } else {
            if (node.treePrev.numNewLines() == 1 || node.treePrev.numNewLines() > 2) {
                WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treePrev as LeafPsiElement).replaceWithText("\n\n")
                }
            }
        }
    }

    private fun checkSpaceBetweenPropertyAndComment(node: ASTNode, configuration: KdocCodeBlocksFormattingConfiguration) {
        if (node.treeParent.elementType == PROPERTY
                && node.treeParent.firstChildNode != node) {
            if (!node.treePrev.isWhiteSpace()) {
                COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    node.treeParent.addChild(PsiWhiteSpaceImpl(" ".repeat(configuration.maxSpaces)), node)
                }
            } else if (node.treePrev.text.length != configuration.maxSpaces) {
                COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treePrev as LeafPsiElement).replaceWithText(" ".repeat(configuration.maxSpaces))
                }
            }
        }
    }

    private fun checkWhiteSpaceBeforeComment(node: ASTNode) {
        if (node.text.trim('/', '*').takeWhile { it == ' ' }.length == APPROPRIATE_COMMENT_SPACES)
            return

        COMMENT_WHITE_SPACE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
            val commentText = node.text.drop(2).trim()

            if (node.text.startsWith("//"))
                (node as LeafPsiElement).replaceWithText("// $commentText")
            else if(node.text.startsWith("/*"))
                (node as LeafPsiElement).replaceWithText("/* $commentText")
        }
    }

    private fun checkClassComment(node: ASTNode) {
        if (isFirstComment(node)) {
            if (node.treeParent.elementType == BLOCK || node.treeParent.elementType == CLASS_BODY)
                checkFirstCommentSpaces(node)
            else
                checkFirstCommentSpaces(node.treeParent)

            return
        }

        if (!node.treeParent.treePrev.isWhiteSpace() && node.treeParent.elementType != FILE) {
            WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                node.treeParent.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent)
            }
        } else if(node.treeParent.elementType != FILE) {
            if (node.treeParent.treePrev.numNewLines() == 1 || node.treeParent.treePrev.numNewLines() > 2) {
                WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treeParent.treePrev as LeafPsiElement).replaceWithText("\n\n")
                }
            }
        }
    }

    private fun checkFirstCommentSpaces(node: ASTNode) {
        if (node.treePrev.isWhiteSpace()) {
            if (node.treePrev.numNewLines() > 1
                    || node.treePrev.numNewLines() == 0) {
                FIRST_COMMENT_NO_SPACES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treePrev as LeafPsiElement).replaceWithText("\n")
                }
            }
        }
    }


    private fun isFirstComment(node: ASTNode): Boolean {
        // In case when comment is inside of a function or class
        if (node.treeParent.elementType == BLOCK || node.treeParent.elementType == CLASS_BODY) {
            return if (node.treePrev.isWhiteSpace())
                node.treePrev.treePrev.elementType == LBRACE
            else
                node.treePrev.elementType == LBRACE
        }

        // When comment inside of a PROPERTY
        if (node.treeParent.elementType != FILE)
            return node.treeParent.treePrev.treePrev.elementType == LBRACE

        return node.treeParent.getAllChildrenWithType(node.elementType).first() == node
    }

    class KdocCodeBlocksFormattingConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxSpaces = config["maxSpaces"]?.toIntOrNull() ?: MAX_SPACES
    }
}
