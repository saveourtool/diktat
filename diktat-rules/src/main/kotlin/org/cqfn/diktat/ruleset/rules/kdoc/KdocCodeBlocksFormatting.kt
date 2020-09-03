package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMMENT_NEW_LINE_ABOVE
import org.cqfn.diktat.ruleset.constants.Warnings.FIRST_COMMENT_NO_SPACES
import org.cqfn.diktat.ruleset.constants.Warnings.WHITESPACE_IN_COMMENT
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

class KdocCodeBlocksFormatting : Rule("kdoc-comments-codeblocks-formatting") {
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

        if (node.elementType == EOL_COMMENT) {
            checkWhiteSpaceBeforeComment(node)
        }

        when (node.elementType) {
            BLOCK -> handleBlockComments(node)
            CLASS_BODY, CLASS -> handleClassComments(node)
        }

    }

    private fun handleClassComments(node: ASTNode) {
        val eolComments = node.getAllChildrenWithType(EOL_COMMENT)
        val kDocComments = node.getAllChildrenWithType(KDOC)

        eolComments.forEach {
            checkClassComment(it)
        }

        kDocComments.forEach {
            checkClassComment(it)
        }
    }

    private fun handleBlockComments(node: ASTNode) {
        val eolComments = node.getAllChildrenWithType(EOL_COMMENT)
        val properties = node.getAllChildrenWithType(PROPERTY)

        eolComments.forEach {
            checkBlockComments(it)
        }

        properties.forEach {
            if (it.hasChildOfType(KDOC)) {
                checkBlockComments(it)
            }
        }
    }

    private fun checkBlockComments(node: ASTNode) {
        if (isFirstComment(node)) {
            checkFirstCommentSpaces(node)
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

    private fun checkWhiteSpaceBeforeComment(node: ASTNode) {
        if (node.text.takeWhile { it == '/' || it == ' ' }.length == 3)
            return

        WHITESPACE_IN_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
            val commentText = node.text.drop(2).trim()

            (node as LeafPsiElement).replaceWithText("// $commentText")
        }
    }

    private fun checkClassComment(node: ASTNode) {
        if (isFirstComment(node)) {
            checkFirstCommentSpaces(node)
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
        if (node.treeParent.treePrev.isWhiteSpace()) {
            if (node.treeParent.treePrev.text.filter { it == '\n' }.length > 1) {
                FIRST_COMMENT_NO_SPACES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset) {
                    node.treeParent.treeParent.removeChild(node.treeParent.treePrev)
                }
            }
        }
    }

    private fun isFirstComment(node: ASTNode): Boolean {
        if (node.treeParent.treeParent.elementType == CLASS_BODY) {
            return node.treeParent == node.treeParent.treeParent.getFirstChildWithType(node.treeParent.elementType)
        }

        if (node.treeParent.elementType == BLOCK) {
            return node == node.treeParent.getFirstChildWithType(node.elementType)
        }
        return false
    }
}