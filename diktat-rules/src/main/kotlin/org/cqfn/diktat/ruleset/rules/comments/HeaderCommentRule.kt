package org.cqfn.diktat.ruleset.rules.comments

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextSibling
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.common.config.rules.isRuleEnabled
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_CONTAINS_DATE_OR_AUTHOR
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_NOT_BEFORE_PACKAGE
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.leaveOnlyOneNewLine
import org.cqfn.diktat.ruleset.utils.moveChildBefore
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Visitor for header comment in .kt file:
 * 1) Ensure header comment is at the very top and properly formatted (has newline after KDoc end)
 * 2) Ensure copyright exists and is properly formatted
 * 3) Ensure there are no dates or authors
 * 4) Ensure files with many or zero classes have proper description
 */
class HeaderCommentRule : Rule("header-comment") {
    private val copyrightWords = setOf("copyright", "版权")

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private var fileName: String = ""

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.rulesConfigList!!
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == FILE) {
            fileName = params.fileName
                ?: ""  // fixme fileName is null only in unit tests, can be fixed to never be null
            checkCopyright(node)
            if (checkHeaderKdocPosition(node)) {
                checkHeaderKdoc(node)
            }
        }
    }

    private fun checkCopyright(node: ASTNode) {
        val configuration = CopyrightConfiguration(configRules.getRuleConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT)?.configuration
            ?: mapOf())
        if (!configuration.isCopyrightMandatory() && !configuration.hasCopyrightText()) return

        val copyrightText = configuration.getCopyrightText()

        val headerComment = node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)
        val isWrongCopyright = headerComment != null && !headerComment.text.contains(copyrightText)
        val isMissingCopyright = headerComment == null && configuration.isCopyrightMandatory()
        val isCopyrightInsideKdoc = (node.getAllChildrenWithType(KDOC) + node.getAllChildrenWithType(ElementType.EOL_COMMENT))
            .any { commentNode ->
                copyrightWords.any { commentNode.text.contains(it, ignoreCase = true) }
            }

        if (isWrongCopyright || isMissingCopyright || isCopyrightInsideKdoc) {
            HEADER_MISSING_OR_WRONG_COPYRIGHT.warnAndFix(configRules, emitWarn, isFixMode, fileName, node.startOffset) {
                if (headerComment != null) {
                    node.removeChild(headerComment)
                }
                // do not insert empty line before header kdoc
                val newLines = if (node.findChildBefore(PACKAGE_DIRECTIVE, KDOC) != null) "\n" else "\n\n"
                node.addChild(LeafPsiElement(WHITE_SPACE, newLines), node.firstChildNode)
                node.addChild(LeafPsiElement(BLOCK_COMMENT,
                    """
                        /*
                         * $copyrightText
                         */
                    """.trimIndent()),
                    node.firstChildNode
                )
            }
        }
    }

    /**
     * If corresponding rule is enabled, checks if header KDoc is positioned correctly and moves it in fix mode.
     * @return true if position check is not needed or if header KDoc is positioned correctly or it was moved by fix mode
     */
    private fun checkHeaderKdocPosition(node: ASTNode): Boolean {
        // AST always has PACKAGE_DIRECTIVE and IMPORT_LIST nodes, even if they are empty
        if (configRules.isRuleEnabled(HEADER_NOT_BEFORE_PACKAGE) && node.findChildBefore(PACKAGE_DIRECTIVE, KDOC) == null) {
            val firstKdoc = node.findChildAfter(IMPORT_LIST, KDOC)
            if (firstKdoc != null && firstKdoc.treeParent.elementType == FILE) {
                HEADER_NOT_BEFORE_PACKAGE.warnAndFix(configRules, emitWarn, isFixMode, fileName, firstKdoc.startOffset) {
                    node.moveChildBefore(firstKdoc, node.getFirstChildWithType(PACKAGE_DIRECTIVE), true)
//                         remove empty line before copyright and header kdoc
                    node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)?.apply {
                        nextSibling { it.elementType == WHITE_SPACE }!!.leaveOnlyOneNewLine()
                    }
                }
                if (!isFixMode) return false
            }
        }
        return true
    }

    private fun checkHeaderKdoc(node: ASTNode) {
        val headerKdoc = node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)
        if (headerKdoc == null) {
            val nDeclaredClasses = node.getAllChildrenWithType(ElementType.CLASS).size
            if (nDeclaredClasses == 0 || nDeclaredClasses > 1) {
                HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warn(configRules, emitWarn, isFixMode, fileName, node.startOffset)
            }
        } else {
            // fixme we should also check date of creation, but it can be in different formats
            headerKdoc.text.split('\n')
                .filter { it.contains("@author") }
                .forEach {
                    HEADER_CONTAINS_DATE_OR_AUTHOR.warn(configRules, emitWarn, isFixMode,
                        it.trim(), headerKdoc.startOffset)
                }

            if (headerKdoc.treeNext != null && headerKdoc.treeNext.elementType == WHITE_SPACE
                && headerKdoc.treeNext.text.count { it == '\n' } != 2) {
                HEADER_WRONG_FORMAT.warnAndFix(configRules, emitWarn, isFixMode,
                    "header KDoc should have a new line after", headerKdoc.startOffset) {
                    node.replaceChild(headerKdoc.treeNext, LeafPsiElement(WHITE_SPACE, "\n\n"))
                }
            }
        }
    }

    class CopyrightConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        fun isCopyrightMandatory() = config["isCopyrightMandatory"]?.toBoolean() ?: false

        fun hasCopyrightText() = config.keys.contains("copyrightText")
        fun getCopyrightText() = config["copyrightText"] ?: error("Copyright is not set in configuration")
    }
}
