package org.cqfn.diktat.ruleset.rules.comments

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMMENTED_OUT_CODE
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.TokenType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

/**
 * This rule performs checks related to all comments in general
 * 1. No commented out code is allowed, including imports
 * 2. Warns if there are files with only comments (todo)
 */
class CommentsRule : Rule("comments") {
    private val IMPORT_OR_PACKAGE = """(import|package) """.toRegex()
    private val EOL_COMMENT_START = """// \S""".toRegex()

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private var ktPsiFactory: KtPsiFactory? = null

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        configRules = params.rulesConfigList!!
        emitWarn = emit
        isFixMode = autoCorrect

        ktPsiFactory = KtPsiFactory(node.psi.project, false)  // regarding markGenerated see KDoc in Kotlin sources
        when (node.elementType) {
            FILE -> checkCommentedCode(node)
        }
    }

    /**
     * This method tries to detect commented code by uncommenting and parsing it. If parser reports errors,
     * we assume this is not code (it is hard to try and parse further because comments can contain code snippets).
     * @implNote
     * 1. Import and package directives should be separated from the rest of uncommented lines
     * 2. Import usually go on top of the file, so if comment contains not only imports, it probably contains imports only on top. (this possibly needs fixme)
     * 3. Code can be surrounded by actual comments. One possible heuristic here is to assume actual comments start
     *    with '// ' with whitespace, while automatic commenting in, e.g., IDEA creates slashes in the beginning of the line
     */
    private fun checkCommentedCode(node: ASTNode) {
        val eolCommentsOffsetToText = getOffsetsToTextBlocksFromEOLComments(node)
        val blockCommentsOffsetToText = node.findAllNodesWithSpecificType(BLOCK_COMMENT).map { it.startOffset to it.text.removeSurrounding("/*", "*/") }

        (eolCommentsOffsetToText + blockCommentsOffsetToText)
            .flatMap { (offset, text) ->
                val (singleLines, blockLines) = text.lines().partition { it.contains(IMPORT_OR_PACKAGE) }
                val block = if (blockLines.isNotEmpty()) listOf(blockLines.joinToString("\n")) else listOf()
                (singleLines + block).map { offset to it }
            }
            .map { (offset, text) ->
                when {
                    text.contains("import") ->
                        offset to ktPsiFactory!!.createImportDirective(ImportPath.fromString(text.substringAfter("import "))).node
                    text.contains("package") ->
                        offset to ktPsiFactory!!.createPackageDirective(FqName(text.substringAfter("package "))).node
                    else ->
                        offset to ktPsiFactory!!.createBlockCodeFragment(text, null).node
                }
            }
            .filter { (_, parsedNode) ->
                parsedNode.findAllNodesWithSpecificType(TokenType.ERROR_ELEMENT).isEmpty()
            }.forEach { (offset, parsedNode) ->
                COMMENTED_OUT_CODE.warn(configRules, emitWarn, isFixMode, parsedNode.text.substringBefore("\n").trim(), offset)
            }
    }

    /**
     * This method is used to extract text from EOL comments in a form which can be used for parsing.
     * Multiple consecutive EOL comments can correspond to one code block, so we try to glue them together here.
     * Splitting back into lines, if necessary, will be done outside of this method, for both text from EOL and block.
     * fixme: in this case offset is lost for lines which will be split once more
     */
    private fun getOffsetsToTextBlocksFromEOLComments(node: ASTNode): List<Pair<Int, String>> {
        val comments = node.findAllNodesWithSpecificType(EOL_COMMENT)
            .filter { !it.text.contains(EOL_COMMENT_START) }
        return if (comments.isNotEmpty()) {
            val result = mutableListOf(mutableListOf(comments.first()))
            comments.drop(1).fold(result) { acc, astNode ->
                val isImportOrPackage = astNode.text.contains(IMPORT_OR_PACKAGE)
                val previousNonWhiteSpaceNode = astNode.prevSibling { it.elementType != WHITE_SPACE }
                if (!isImportOrPackage && previousNonWhiteSpaceNode in acc.last()) {
                    acc.last().add(astNode)
                } else {
                    acc.add(mutableListOf(astNode))
                }
                acc
            }.map { list ->
                list.first().startOffset to list.joinToString("\n") { it.text.removePrefix("//") }
            }
        } else {
            listOf()
        }
    }
}
