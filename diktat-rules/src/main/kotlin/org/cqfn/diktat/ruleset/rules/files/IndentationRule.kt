package org.cqfn.diktat.ruleset.rules.files

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.LBRACKET
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACKET
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.visit
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.getAllLLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.indentation.CustomIndentationChecker
import org.cqfn.diktat.ruleset.utils.indentation.ExpressionIndentationChecker
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig
import org.cqfn.diktat.ruleset.utils.indentation.ValueParameterListChecker
import org.cqfn.diktat.ruleset.utils.leaveOnlyOneNewLine
import org.cqfn.diktat.ruleset.utils.log
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult

/**
 * Rule that checks indentation. The following general rules are checked:
 * 1. Only spaces should be used each indentation is equal to 4 spaces
 * 2. File should end with new line
 * Additionally, a set of CustomIndentationChecker objects checks all WHITE_SPACE node if they are exceptions from general rules.
 * @see CustomIndentationChecker
 */
class IndentationRule : Rule("indentation") {
    companion object {
        const val INDENT_SIZE = 4
        private val increasingTokens = listOf(LPAR, LBRACE, LBRACKET)
        private val decreasingTokens = listOf(RPAR, RBRACE, RBRACKET)
    }

    private lateinit var configuration: IndentationConfig
    private lateinit var customIndentationCheckers: List<CustomIndentationChecker>

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private var fileName: String = ""

    override fun visit(node: ASTNode, autoCorrect: Boolean, params: KtLint.Params, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        isFixMode = autoCorrect
        emitWarn = emit
        fileName = params.fileName!!

        configuration = IndentationConfig(configRules.getRuleConfig(WRONG_INDENTATION)?.configuration
                ?: mapOf())
        customIndentationCheckers = listOf(
                ValueParameterListChecker(configuration),
                ExpressionIndentationChecker(configuration)
        )

        if (node.elementType == FILE) {
            if (checkIsIndentedWithSpaces(node)) {
                checkIndentation(node)
            } else {
                log.warn("Not going to check indentation because there are tabs")
            }
            checkNewlineAtEnd(node)
        }
    }

    /**
     * This method warns if tabs are used in WHITE_SPACE nodes and substitutes them with spaces in fix mode
     */
    private fun checkIsIndentedWithSpaces(node: ASTNode): Boolean {
        val whiteSpaceNodes = mutableListOf<ASTNode>()
        node.getAllLLeafsWithSpecificType(WHITE_SPACE, whiteSpaceNodes)
        whiteSpaceNodes
                .filter { it.textContains('\t') }
                .apply { if (isEmpty()) return true }
                .forEach {
                    WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, fileName, it.startOffset + it.text.indexOf('\t')) {
                        (it as LeafPsiElement).replaceWithText(it.text.replace("\t", " ".repeat(INDENT_SIZE)))
                    }
                }
        return isFixMode  // true if we changed all tabs to spaces
    }

    private fun checkIndentation(node: ASTNode) {
        var indents = 0
        val whiteSpaceNodes = mutableMapOf<PsiWhiteSpace, IndentationError>()
        node.visit { astNode ->
            when (astNode.elementType) {
                in increasingTokens -> indents += INDENT_SIZE
                in decreasingTokens -> {
                    // indents are corrected when we handle WHITE_SPACE with \n which is before decreasingToken
                    if (astNode.treePrev.elementType != WHITE_SPACE || !astNode.treePrev.textContains('\n')) {
                        indents -= INDENT_SIZE
                    }
                }
                WHITE_SPACE -> {
                    if (astNode.textContains('\n') && astNode.treeNext != null) {
                        if (astNode.treeNext.elementType in decreasingTokens) {
                            indents -= INDENT_SIZE
                        }

                        val actualIndent = astNode.text.substringAfterLast('\n').count { it == ' ' }
                        whiteSpaceNodes.putIfAbsent(astNode.psi as PsiWhiteSpace, IndentationError(indents, actualIndent))
                    }
                }
            }
        }

        whiteSpaceNodes.forEach { (whiteSpace, indentError) ->
            val checkResult = customIndentationCheckers.firstNotNullResult {
                it.checkNode(whiteSpace, indentError)
            }

            val expectedIndent = checkResult?.expected ?: indentError.expected
            if (checkResult?.correct != true && expectedIndent != indentError.actual) {
                WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode,
                        "expected $expectedIndent but was ${indentError.actual}",
                        whiteSpace.startOffset + whiteSpace.text.lastIndexOf('\n') + 1) {
                    whiteSpace.indentBy(expectedIndent)
                }
            }
        }
    }

    private fun PsiWhiteSpace.indentBy(indent: Int) {
        // fixme this should be done in separate exception handler
        val adj = if (nextSibling.node.elementType in listOf(KDOC_LEADING_ASTERISK, KDOC_END, KDOC_SECTION)) 1 else 0
        (node as LeafPsiElement).rawReplaceWithText(text.substringBeforeLast('\n') + "\n" + " ".repeat(indent + adj))
    }

    private fun checkNewlineAtEnd(node: ASTNode) {
        if (configuration.newlineAtEnd) {
            val lastChild = node.lastChildNode
            if (lastChild.elementType != WHITE_SPACE || lastChild.text.count { it == '\n' } != 1) {
                WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, fileName, node.startOffset + node.textLength) {
                    if (lastChild.elementType != WHITE_SPACE) {
                        node.addChild(PsiWhiteSpaceImpl("\n"), null)
                    } else {
                        lastChild.leaveOnlyOneNewLine()
                    }
                }
            }
        }
    }

    internal data class IndentationError(val expected: Int, val actual: Int)
}
