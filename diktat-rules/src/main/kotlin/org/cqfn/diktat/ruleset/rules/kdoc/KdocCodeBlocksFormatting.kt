package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMMENT_NEW_LINE_ABOVE
import org.cqfn.diktat.ruleset.constants.Warnings.FIRST_COMMENT_NO_SPACES
import org.cqfn.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.SPACE_BETWEEN_COMMENT_AND_CODE
import org.cqfn.diktat.ruleset.constants.Warnings.WHITESPACE_IN_COMMENT
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl


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
class KdocCodeBlocksFormatting : Rule("kdoc-comments-codeblocks-formatting") {

    companion object {
        private const val MAX_SPACES = 1
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        configRules = params.getDiktatConfigRules()
        isFixMode = autoCorrect
        emitWarn = emit

        val configuration = KdocCodeBlocksFormattingConfiguration(
                configRules.getRuleConfig(SPACE_BETWEEN_COMMENT_AND_CODE)?.configuration ?: mapOf())

        when (node.elementType) {
            IF -> {
                handleIfElse(node)
            }
            EOL_COMMENT -> {
                checkSpaceBetweenPropertyAndComment(node, configuration)

                if (node.treeParent.elementType == BLOCK && node.treeParent.lastChildNode != node) {
                    checkBlockComments(node)
                } else if (node.treeParent.lastChildNode != node && node.treeParent.elementType != IF) {
                    checkClassComment(node)
                }

                checkWhiteSpaceBeforeComment(node)
            }
            BLOCK_COMMENT -> {
                checkSpaceBetweenPropertyAndComment(node, configuration)

                if (node.treeParent.elementType == BLOCK && node.treeParent.lastChildNode != node) {
                    checkBlockComments(node)
                } else if (node.treeParent.lastChildNode != node && node.treeParent.elementType != IF) {
                    checkClassComment(node)
                }
            }

            KDOC -> {
                if (node.treeParent.treeParent.elementType == BLOCK) {
                    checkBlockComments(node.treeParent) // node.treeParent is a Property.
                } else if (node.treeParent.elementType != IF){
                    checkClassComment(node)
                }
            }
        }

    }

    private fun handleIfElse(node: ASTNode) {
        if(node.hasChildOfType(ELSE)) {
            val elseKeyWord = node.getFirstChildWithType(ELSE_KEYWORD)!!
            if(elseKeyWord.treePrev.treePrev.elementType == EOL_COMMENT ||
                    elseKeyWord.treePrev.treePrev.elementType == KDOC ||
                    elseKeyWord.treePrev.treePrev.elementType == BLOCK_COMMENT) {
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

    private fun checkBlockComments(node: ASTNode) {
        if (isFirstComment(node)) {
            if (node.treeParent.elementType == BLOCK || node.treeParent.elementType == CLASS_BODY)
                checkFirstCommentSpaces(node)
            else
                checkFirstCommentSpaces(node.treeParent)

            return
        }

        if (!node.treePrev.isWhiteSpace()) {
            COMMENT_NEW_LINE_ABOVE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                node.treeParent.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent)
            }
        } else {
            if (node.treePrev.text.filter { it == '\n' }.length == 1 || node.treePrev.text.filter { it == '\n' }.length > 2) {
                COMMENT_NEW_LINE_ABOVE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treeParent.treePrev as LeafPsiElement).replaceWithText("\n\n")
                }
            }
        }
    }

    private fun checkSpaceBetweenPropertyAndComment(node: ASTNode, configuration: KdocCodeBlocksFormattingConfiguration) {
        if (node.treeParent.elementType == PROPERTY
                && node.treeParent.firstChildNode != node) {
            if (!node.treePrev.isWhiteSpace()) {
                SPACE_BETWEEN_COMMENT_AND_CODE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    node.treeParent.addChild(PsiWhiteSpaceImpl(" ".repeat(configuration.maxSpaces)), node)
                }
            } else if (node.treePrev.text.length != configuration.maxSpaces) {
                SPACE_BETWEEN_COMMENT_AND_CODE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treePrev as LeafPsiElement).replaceWithText(" ".repeat(configuration.maxSpaces))
                }
            }
        }
    }

    private fun checkWhiteSpaceBeforeComment(node: ASTNode) {
        if (node.text.takeWhile { it == '/' || it == ' ' || it == '*' }.length == 3)
            return

        WHITESPACE_IN_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
            val commentText = node.text.drop(2).trim()

            (node as LeafPsiElement).replaceWithText("// $commentText")
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

        if (!node.treeParent.treePrev.isWhiteSpace()) {
            COMMENT_NEW_LINE_ABOVE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                node.treeParent.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeParent)
            }
        } else {
            if (node.treeParent.treePrev.text.filter { it == '\n' }.length == 1 || node.treeParent.treePrev.text.filter { it == '\n' }.length > 2) {
                COMMENT_NEW_LINE_ABOVE.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treeParent.treePrev as LeafPsiElement).replaceWithText("\n\n")
                }
            }
        }
    }

    private fun checkFirstCommentSpaces(node: ASTNode) {
        if (node.treePrev.isWhiteSpace()) {
            if (node.treePrev.text.filter { it == '\n' }.length > 1) {
                FIRST_COMMENT_NO_SPACES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    (node.treePrev as LeafPsiElement).replaceWithText("\n")
                }
            }
        }
    }


    private fun isFirstComment(node: ASTNode): Boolean {
        if (node.treeParent.elementType == BLOCK || node.treeParent.elementType == CLASS_BODY) {
            return if (node.treePrev.isWhiteSpace())
                node.treePrev.treePrev.elementType == LBRACE
            else
                node.treePrev.elementType == LBRACE
        }

        return node.treeParent.treePrev.treePrev.elementType == LBRACE
    }

    class KdocCodeBlocksFormattingConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxSpaces = config["maxSpaces"]?.toIntOrNull() ?: MAX_SPACES
    }
}
