package com.saveourtool.diktat.ruleset.rules.chapter2.comments

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_NOT_BEFORE_PACKAGE
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_COPYRIGHT_YEAR
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.copyrightWords
import com.saveourtool.diktat.ruleset.utils.findChildAfter
import com.saveourtool.diktat.ruleset.utils.findChildBefore
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFilePath
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.isGradleScript
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.moveChildBefore

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_LIST
import org.jetbrains.kotlin.KtNodeTypes.PACKAGE_DIRECTIVE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import java.time.LocalDate

/**
 * Visitor for header comment in .kt file:
 * 1) Ensure header comment is at the very top and properly formatted (has newline after KDoc end)
 * 2) Ensure copyright exists and is properly formatted
 * 3) Ensure there are no dates or authors
 * 4) Ensure files with many or zero classes have proper description
 */
@Suppress("ForbiddenComment")
class HeaderCommentRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE, HEADER_MISSING_OR_WRONG_COPYRIGHT, HEADER_NOT_BEFORE_PACKAGE,
        HEADER_NOT_BEFORE_PACKAGE, HEADER_WRONG_FORMAT, WRONG_COPYRIGHT_YEAR),
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE && !node.getFilePath().isGradleScript()) {
            checkCopyright(node)
            if (checkHeaderKdocPosition(node)) {
                checkHeaderKdoc(node)
            }
        }
    }

    private fun checkHeaderKdoc(node: ASTNode) {
        node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)?.let { headerKdoc ->
            if (headerKdoc.treeNext != null && headerKdoc.treeNext.elementType == WHITE_SPACE &&
                    headerKdoc.treeNext.text.count { it == '\n' } != 2) {
                HEADER_WRONG_FORMAT.warnAndFix(configRules, emitWarn, isFixMode,
                    "header KDoc should have a new line after", headerKdoc.startOffset, headerKdoc) {
                    node.replaceChild(headerKdoc.treeNext, PsiWhiteSpaceImpl("\n\n"))
                }
            }
        }
            ?: run {
                val numDeclaredClassesAndObjects = node.getAllChildrenWithType(KtNodeTypes.CLASS).size +
                        node.getAllChildrenWithType(KtNodeTypes.OBJECT_DECLARATION).size
                if (numDeclaredClassesAndObjects != 1) {
                    HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warn(configRules, emitWarn,
                        "there are $numDeclaredClassesAndObjects declared classes and/or objects", node.startOffset, node)
                }
            }
    }

    /**
     * If corresponding rule is enabled, checks if header KDoc is positioned correctly and moves it in fix mode.
     * Algorithm is as follows: if there is no KDoc at the top of file (before package directive) and the one after imports
     * isn't bound to any identifier, than this KDoc is misplaced header KDoc.
     *
     * @return true if position check is not needed or if header KDoc is positioned correctly or it was moved by fix mode
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun checkHeaderKdocPosition(node: ASTNode): Boolean {
        val firstKdoc = node.findChildAfter(IMPORT_LIST, KDOC)
        // if `firstKdoc.treeParent` is File then it's a KDoc not bound to any other structures
        if (node.findChildBefore(PACKAGE_DIRECTIVE, KDOC) == null && firstKdoc != null && firstKdoc.treeParent.elementType == KtFileElementType.INSTANCE) {
            HEADER_NOT_BEFORE_PACKAGE.warnAndFix(configRules, emitWarn, isFixMode, "header KDoc is located after package or imports", firstKdoc.startOffset, firstKdoc) {
                node.moveChildBefore(firstKdoc, node.getFirstChildWithType(PACKAGE_DIRECTIVE), true)
                // ensure there is no empty line between copyright and header kdoc
                node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)?.apply {
                    if (treeNext.elementType == WHITE_SPACE) {
                        node.replaceChild(treeNext, PsiWhiteSpaceImpl("\n"))
                    } else {
                        node.addChild(PsiWhiteSpaceImpl("\n"), this.treeNext)
                    }
                }
            }
            if (!isFixMode) {
                return false
            }
        }
        return true
    }

    private fun makeCopyrightCorrectYear(copyrightText: String): String {
        val hyphenYear = hyphenRegex.find(copyrightText)

        hyphenYear?.let {
            val copyrightYears = hyphenYear.value.split("-")
            if (copyrightYears[1].toInt() != curYear) {
                val validYears = "${copyrightYears[0]}-$curYear"
                return copyrightText.replace(hyphenRegex, validYears)
            }
        }

        val afterCopyrightYear = afterCopyrightRegex.find(copyrightText)
        val copyrightYears = afterCopyrightYear?.value?.split("(c)", "(C)", "©")
        return if (copyrightYears != null && copyrightYears[1].trim().toInt() != curYear) {
            val validYears = "${copyrightYears[0]}-$curYear"
            copyrightText.replace(afterCopyrightRegex, validYears)
        } else {
            ""
        }
    }

    @Suppress("TOO_LONG_FUNCTION", "ComplexMethod")
    private fun checkCopyright(node: ASTNode) {
        val configuration = CopyrightConfiguration(configRules.getRuleConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT)?.configuration
            ?: emptyMap())
        if (!configuration.isCopyrightMandatory() && !configuration.hasCopyrightText()) {
            return
        }

        // need to make sure that copyright year is consistent with current year
        val copyrightText = configuration.getCopyrightText()
        val copyrightWithCorrectYear = makeCopyrightCorrectYear(copyrightText)

        if (copyrightWithCorrectYear.isNotEmpty()) {
            log.warn { "Copyright year in your configuration file is not up to date." }
        }

        val headerComment = node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)
        // Depends only on content and doesn't consider years
        val isCopyrightMatchesPatternExceptFirstYear = isCopyRightTextMatchesPattern(headerComment, copyrightText) ||
                isCopyRightTextMatchesPattern(headerComment, copyrightWithCorrectYear)

        val isWrongCopyright = headerComment != null &&
                !isCopyrightMatchesPatternExceptFirstYear &&
                !isHeaderCommentContainText(headerComment, copyrightText) &&
                !isHeaderCommentContainText(headerComment, copyrightWithCorrectYear)

        val isMissingCopyright = headerComment == null && configuration.isCopyrightMandatory()
        val isCopyrightInsideKdoc = (node.getAllChildrenWithType(KDOC) + node.getAllChildrenWithType(KtTokens.EOL_COMMENT))
            .any { commentNode ->
                copyrightWords.any { commentNode.text.contains(it, ignoreCase = true) }
            }

        if (isWrongCopyright || isMissingCopyright || isCopyrightInsideKdoc) {
            val freeText = when {
                // If `isCopyrightInsideKdoc` then `isMissingCopyright` is true too, but warning text from `isCopyrightInsideKdoc` is preferable.
                isCopyrightInsideKdoc -> "copyright is placed inside KDoc, but should be inside a block comment"
                isWrongCopyright -> "copyright comment doesn't have correct copyright text"
                isMissingCopyright -> "copyright is mandatory, but is missing"
                else -> error("Should never get to this point")
            }
            HEADER_MISSING_OR_WRONG_COPYRIGHT.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
                headerComment?.let { copyrightNode ->
                    // remove node clearly, with trailing whitespace
                    if (copyrightNode.treeNext.isWhiteSpace()) {
                        node.removeChild(copyrightNode.treeNext)
                    }
                    node.removeChild(copyrightNode)
                }
                // do not insert empty line before header kdoc
                val newLines = node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)?.let { "\n" } ?: "\n\n"
                node.addChild(PsiWhiteSpaceImpl(newLines), node.firstChildNode)
                node.addChild(LeafPsiElement(BLOCK_COMMENT,
                    """
                        |/*
                        |${handleMultilineCopyright(copyrightWithCorrectYear.ifEmpty { copyrightText })}
                        |*/
                    """.trimMargin()),
                    node.firstChildNode
                )
            }
        }

        // Triggers when there is a copyright, but its year is not updated.
        if (!isMissingCopyright && !isWrongCopyright && copyrightWithCorrectYear.isNotEmpty()) {
            WRONG_COPYRIGHT_YEAR.warnAndFix(configRules, emitWarn, isFixMode, "year should be $curYear", node.startOffset, node) {
                (headerComment as LeafElement).rawReplaceWithText(headerComment.text.replace(copyrightText, copyrightWithCorrectYear))
            }
        }
    }

    private fun isHeaderCommentContainText(headerComment: ASTNode, text: String): Boolean = if (text.isNotEmpty()) headerComment.text.flatten().contains(text.flatten()) else false

    // Check if provided copyright node differs only in the first date from pattern
    private fun isCopyRightTextMatchesPattern(copyrightNode: ASTNode?, copyrightPattern: String): Boolean {
        val copyrightText = copyrightNode?.text
            ?.replace("/*", "")
            ?.replace("*/", "")
            ?.replace("*", "")

        val datesInPattern = hyphenRegex.find(copyrightPattern)?.value
        val datesInCode = copyrightText?.let { hyphenRegex.find(it)?.value }

        if (datesInPattern == null || datesInCode == null) {
            return false
        }

        val patternWithoutDates = copyrightPattern.replace(datesInPattern, "").flatten()
        val textWithoutDates = copyrightText.replace(datesInCode, "").flatten()

        // Text should be equal, first date could be different, second date should be equal to current year
        return (patternWithoutDates == textWithoutDates) && (datesInCode.substringAfter("-") == curYear.toString())
    }

    /**
     * Deletes all spaces and newlines
     * Used to compare copyrights in yaml and file
     */
    private fun String.flatten(): String =
        replace("\n", "")
            .replace(" ", "")

    /**
     * If it is multiline copyright, this method deletes spaces in empty lines.
     * Otherwise, if it is one line copyright, it returns it with 4 spaces at the beginning.
     */
    private fun handleMultilineCopyright(copyrightText: String): String {
        if (copyrightText.startsWith(" ")) {
            return copyrightText
                .lines()
                .dropWhile { it.isBlank() }
                .reduce { acc, nextLine ->
                    when {
                        nextLine.isBlank() -> "$acc\n"
                        else -> "$acc\n$nextLine"
                    }
                }
        }

        return "    $copyrightText"
    }

    /**
     * Configuration for copyright
     */
    class CopyrightConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * @return Whether the copyright is mandatory in all files
         */
        fun isCopyrightMandatory() = config["isCopyrightMandatory"]?.toBoolean() ?: false

        /**
         * Whether copyright text is present in the configuration
         *
         * @return true if config has "copyrightText"
         */
        internal fun hasCopyrightText() = config.keys.contains("copyrightText")

        /**
         * @return text of copyright as configured in the configuration file
         */
        fun getCopyrightText() = config["copyrightText"]?.replace(CURR_YEAR_PATTERN, curYear.toString())
            ?: error("Copyright is not set in configuration")
    }

    companion object {
        private val log = KotlinLogging.logger {}
        const val CURR_YEAR_PATTERN = ";@currYear;"
        const val NAME_ID = "header-comment"
        val hyphenRegex = Regex("""\d+-\d+""")
        val afterCopyrightRegex = Regex("""((©|\([cC]\))+ *\d+)""")
        val curYear = LocalDate.now().year
    }
}
