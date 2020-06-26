package org.diktat.ruleset.rules.comments

import org.diktat.ruleset.constants.Warnings.HEADER_CONTAINS_DATE_OR_AUTHOR
import org.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import org.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import org.diktat.ruleset.utils.findChildBefore
import org.diktat.ruleset.utils.getAllChildrenWithType
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.diktat.common.config.rules.RuleConfiguration
import org.diktat.common.config.rules.RulesConfig
import org.diktat.common.config.rules.getRuleConfig
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

    private lateinit var confiRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private var fileName: String = ""

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        confiRules = params.rulesConfigList!!
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == ElementType.FILE) {
            fileName = params.fileName ?: ""
            checkCopyright(node)
            checkHeaderKdoc(node)
        }
    }

    private fun checkCopyright(node: ASTNode) {
        val configuration = CopyrightConfiguration(confiRules.getRuleConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT)?.configuration
            ?: mapOf())
        val copyrightText = configuration.getCopyrightText()

        val headerComment = node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)
        val isWrongCopyright = headerComment != null && !headerComment.text.contains(copyrightText)
        val isMissingCopyright = headerComment == null && configuration.isCopyrightMandatory()
        val isCopyrightInsideKdoc = (node.getAllChildrenWithType(KDOC) + node.getAllChildrenWithType(ElementType.EOL_COMMENT))
            .any { commentNode ->
                copyrightWords.any { commentNode.text.contains(it, ignoreCase = true) }
            }

        if (isWrongCopyright || isMissingCopyright || isCopyrightInsideKdoc) {
            HEADER_MISSING_OR_WRONG_COPYRIGHT.warnAndFix(confiRules, emitWarn, isFixMode, fileName, node.startOffset) {
                if (headerComment != null) {
                    node.removeChild(headerComment)
                }
                node.addChild(LeafPsiElement(WHITE_SPACE, "\n\n"), node.firstChildNode)
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

    private fun checkHeaderKdoc(node: ASTNode) {
        val headerKdoc = node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)
        if (headerKdoc == null) {
            val nDeclaredClasses = node.getAllChildrenWithType(ElementType.CLASS).size
            if (nDeclaredClasses == 0 || nDeclaredClasses > 1) {
                HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warn(confiRules, emitWarn, isFixMode, fileName, node.startOffset)
            }
        } else {
            // fixme we should also check date of creation, but it can be in different formats
            headerKdoc.text.split('\n')
                .filter { it.contains("@author") }
                .forEach {
                    HEADER_CONTAINS_DATE_OR_AUTHOR.warn(confiRules, emitWarn, isFixMode,
                        it.trim(), headerKdoc.startOffset)
                }

            if (headerKdoc.treeNext != null && headerKdoc.treeNext.elementType == WHITE_SPACE
                && headerKdoc.treeNext.text.count { it == '\n' } != 2) {
                HEADER_WRONG_FORMAT.warnAndFix(confiRules, emitWarn, isFixMode,
                    "header KDoc should have a new line after", headerKdoc.startOffset) {
                    node.replaceChild(headerKdoc.treeNext, LeafPsiElement(WHITE_SPACE, "\n\n"))
                }
            }
        }
    }

    class CopyrightConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        fun isCopyrightMandatory() = config["isCopyrightMandatory"]?.toBoolean() ?: false

        fun getCopyrightText() = config["copyrightText"] ?: error("Copyright is not set in configuration")
    }
}
