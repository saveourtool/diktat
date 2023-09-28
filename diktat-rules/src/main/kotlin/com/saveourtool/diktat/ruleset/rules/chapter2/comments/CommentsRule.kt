package com.saveourtool.diktat.ruleset.rules.chapter2.comments

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.COMMENTED_OUT_CODE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getFilePath
import com.saveourtool.diktat.ruleset.utils.prevSibling

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.TokenType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import org.jetbrains.kotlin.resolve.ImportPath

private typealias ListOfPairs = MutableList<Pair<ASTNode, String>>

/**
 * This rule performs checks if there is any commented code.
 * No commented out code is allowed, including imports.
 */
@Suppress("ForbiddenComment")
class CommentsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(COMMENTED_OUT_CODE)
) {
    private lateinit var ktPsiFactory: KtPsiFactory

    override fun logic(node: ASTNode) {
        ktPsiFactory = KtPsiFactory(node.psi.project, false)  // regarding markGenerated see KDoc in Kotlin sources
        if (node.elementType == KtFileElementType.INSTANCE) {
            checkCommentedCode(node)
        }
    }

    /**
     * This method tries to detect commented code by uncommenting and parsing it. If parser reports errors,
     * we assume this is not code (it is hard to try and parse further because comments can contain code snippets).
     *
     * @implNote
     * 1. Import and package directives should be separated from the rest of uncommented lines
     * 2. Import usually go on top of the file, so if comment contains not only imports, it probably contains imports only on top. (this possibly needs fixme)
     * 3. Code can be surrounded by actual comments. One possible heuristic here is to assume actual comments start
     *    with '// ' with whitespace, while automatic commenting in, e.g., IDEA creates slashes in the beginning of the line
     *
     */
    @Suppress(
        "UnsafeCallOnNullableType",
        "TOO_LONG_FUNCTION",
        "AVOID_NULL_CHECKS"
    )
    private fun checkCommentedCode(node: ASTNode) {
        val errorNodesWithText: ListOfPairs = mutableListOf()
        val eolCommentsOffsetToText = getOffsetsToTextBlocksFromEolComments(node, errorNodesWithText)
        val blockCommentsOffsetToText = node
            .findAllDescendantsWithSpecificType(BLOCK_COMMENT)
            .map {
                errorNodesWithText.add(it to it.text.trim().removeSurrounding("/*", "*/"))
                it.startOffset to it.text.trim().removeSurrounding("/*", "*/")
            }
        (eolCommentsOffsetToText + blockCommentsOffsetToText)
            .flatMap { (offset, text) ->
                val (singleLines, blockLines) = text.lines().partition { it.contains(importOrPackage) }
                val block = if (blockLines.isNotEmpty()) listOf(blockLines.joinToString("\n")) else emptyList()
                (singleLines + block).map {
                    offset to it
                }
            }
            .map { (offset, text) -> offset to text.trim() }
            .mapNotNull { (offset, text) ->
                when {
                    text.isPossibleImport() ->
                        offset to ktPsiFactory.createImportDirective(ImportPath.fromString(text.substringAfter(importKeywordWithSpace, ""))).node
                    text.trimStart().startsWith(packageKeywordWithSpace) ->
                        offset to ktPsiFactory.createPackageDirective(FqName(text.substringAfter(packageKeywordWithSpace, ""))).node
                    else -> if (isContainingRequiredPartOfCode(text)) {
                        offset to ktPsiFactory.createBlockCodeFragment(text, null).node
                    } else {
                        null
                    }
                }
            }
            .filter { (_, parsedNode) ->
                parsedNode
                    .findAllDescendantsWithSpecificType(TokenType.ERROR_ELEMENT)
                    .isEmpty()
            }
            .forEach { (offset, parsedNode) ->
                val invalidNode = errorNodesWithText.find {
                    it.second.trim().contains(parsedNode.text, false) ||
                            parsedNode.text.contains(it.second.trim(), false)
                }?.first
                if (invalidNode == null) {
                    logger.warn {
                        "Text [${parsedNode.text}] is a piece of code, created from comment; " +
                                "but no matching text in comments has been found in the file ${node.getFilePath()}"
                    }
                } else {
                    COMMENTED_OUT_CODE.warn(
                        configRules,
                        emitWarn,
                        parsedNode.text.substringBefore("\n").trim(),
                        offset,
                        invalidNode
                    )
                }
            }
    }

    /**
     * This method is used to extract text from EOL comments in a form which can be used for parsing.
     * Multiple consecutive EOL comments can correspond to one code block, so we try to glue them together here.
     * Splitting back into lines, if necessary, will be done outside of this method, for both text from EOL and block.
     * fixme: in this case offset is lost for lines which will be split once more
     */
    private fun getOffsetsToTextBlocksFromEolComments(node: ASTNode, errorNodesWithText: ListOfPairs): List<Pair<Int, String>> {
        val comments = node
            .findAllDescendantsWithSpecificType(EOL_COMMENT)
            .filter { !it.text.contains(eolCommentStart) || isCodeAfterCommentStart(it.text) }
        return if (comments.isNotEmpty()) {
            val result = mutableListOf(mutableListOf(comments.first()))
            comments
                .drop(1)
                .fold(result) { acc, astNode ->
                    val isImportOrPackage = astNode.text.contains(importOrPackage)
                    val previousNonWhiteSpaceNode = astNode.prevSibling { it.elementType != WHITE_SPACE }
                    if (!isImportOrPackage && previousNonWhiteSpaceNode in acc.last()) {
                        acc.last().add(astNode)
                    } else {
                        acc.add(mutableListOf(astNode))
                    }
                    acc
                }
                .map { list ->
                    list.forEach { errorNodesWithText.add(it to it.text.removePrefix("//")) }
                    list.first().startOffset to list.joinToString("\n") { it.text.removePrefix("//") }
                }
        } else {
            emptyList()
        }
    }

    /**
     * This is a very rare case. We should check this cases for 4 things:
     *
     * 1. If it is a class/object at the beginning of the line
     * 2. If it is a function
     * 3. If it is import/package implementation
     * 4. If it is }. This case is used when } goes after one space and it is closing class or fun
     */
    private fun isCodeAfterCommentStart(text: String): Boolean {
        val textWithoutCommentStartToken = text.removePrefix("//").trim()
        return codeFileStartCases.any { textWithoutCommentStartToken.contains(it) }
    }

    private fun isContainingRequiredPartOfCode(text: String): Boolean =
        text.contains("val ", true) || text.contains("var ", true) || text.contains("=", true) || (text.contains("{", true) && text.substringAfter("{").contains("}", true))

    /**
     * Some weak checks to see if this string can be used as a part of import statement.
     * Only string surrounded in backticks or a dot-qualified expression (i.e., containing words maybe separated by dots)
     * are considered for this case.
     */
    private fun String.isPossibleImport(): Boolean = trimStart().startsWith(importKeywordWithSpace) &&
            substringAfter(importKeywordWithSpace, "").run {
                startsWith('`') && endsWith('`') || !contains(' ')
            }

    @Suppress("MaxLineLength")
    companion object {
        const val NAME_ID = "comments"
        private val logger = KotlinLogging.logger {}
        private val importKeywordWithSpace = "${KtTokens.IMPORT_KEYWORD.value} "
        private val packageKeywordWithSpace = "${KtTokens.PACKAGE_KEYWORD.value} "
        private val importOrPackage = """($importKeywordWithSpace|$packageKeywordWithSpace)""".toRegex()
        private val classRegex =
            """^\s*(public|private|protected)*\s*(internal)*\s*(open|data|sealed)*\s*(internal)*\s*(class|object)\s+(\w+)(\(.*\))*(\s*:\s*\w+(\(.*\))*)?\s*\{*$""".toRegex()
        private val importOrPackageRegex = """^(import|package)?\s+([a-zA-Z.])+;*$""".toRegex()
        private val functionRegex = """^(public|private|protected)*\s*(override|abstract|actual|expect)*\s?fun\s+\w+(\(.*\))?(\s*:\s*\w+)?\s*[{=]${'$'}""".toRegex()
        private val rightBraceRegex = """^\s*}$""".toRegex()
        private val valOrVarRegex = """val |var """.toRegex()
        private val codeFileStartCases = listOf(classRegex, importOrPackageRegex, functionRegex, rightBraceRegex, valOrVarRegex)
        private val eolCommentStart = """// \S""".toRegex()
    }
}
