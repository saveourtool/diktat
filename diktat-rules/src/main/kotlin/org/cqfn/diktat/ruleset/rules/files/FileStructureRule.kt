package org.cqfn.diktat.ruleset.rules.files

import com.google.common.annotations.VisibleForTesting
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE_ANNOTATION_LIST
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_WILDCARD_IMPORTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_CONTAINS_ONLY_COMMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_INCORRECT_BLOCKS_ORDER
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_UNORDERED_IMPORTS
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.isChildAfterAnother
import org.cqfn.diktat.ruleset.utils.isChildBeforeAnother
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Visitor for checking internal file structure.
 * 1. Checks file contains not only comments
 * 2. Ensures the following blocks order: Copyright, Header Kdoc, @file annotation, package name, Import statements,
 *    top class header and top function header comments, top-level classes or top-level functions
 * 3. Ensures there is a blank line between these blocks
 * 4. Ensures imports are ordered alphabetically without blank lines
 * 5. Ensures there are no wildcard imports
 */
class FileStructureRule : Rule("file-structure") {

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private var fileName: String = ""

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        configRules = params.getDiktatConfigRules()
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == ElementType.FILE) {
            fileName = params.fileName ?: ""
            if (checkFileHasCode(node)) {
                checkCodeBlocksOrderAndEmptyLines(node)
            }
            checkImportsOrder(node.findChildByType(IMPORT_LIST)!!)
        }
    }

    private fun checkFileHasCode(node: ASTNode): Boolean {
        val codeTokens = TokenSet.andNot(TokenSet.ANY, TokenSet.create(WHITE_SPACE, KDOC, BLOCK_COMMENT, EOL_COMMENT, PACKAGE_DIRECTIVE, IMPORT_LIST))
        val hasCode = node.getChildren(codeTokens).isNotEmpty()
        if (!hasCode) {
            FILE_CONTAINS_ONLY_COMMENTS.warn(configRules, emitWarn, isFixMode, fileName, node.startOffset)
        }
        return hasCode
    }

    private fun checkCodeBlocksOrderAndEmptyLines(node: ASTNode) {
        val copyrightComment = node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)
        val headerKdoc = node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)
        val fileAnnotations = node.findChildByType(FILE_ANNOTATION_LIST)
        // the following two nodes are always present, even if their content is empty
        // also kotlin compiler itself enforces their position in the file
        val packageDirectiveNode = node.findChildByType(PACKAGE_DIRECTIVE)!!
        val importsList = node.findChildByType(IMPORT_LIST)!!

        // checking order
        listOfNotNull(copyrightComment, headerKdoc, fileAnnotations).forEach { astNode ->
            val (afterThisNode, beforeThisNode) = astNode.getSiblingBlocks(copyrightComment, headerKdoc, fileAnnotations, packageDirectiveNode)
            val isPositionIncorrect = (afterThisNode != null && !node.isChildAfterAnother(astNode, afterThisNode))
                    || !node.isChildBeforeAnother(astNode, beforeThisNode)

            if (isPositionIncorrect) {
                FILE_INCORRECT_BLOCKS_ORDER.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(), astNode.startOffset) {
                    node.moveChildBefore(astNode, beforeThisNode, 2)
                }
            }
        }

        // checking empty lines
        arrayOf(copyrightComment, headerKdoc, fileAnnotations, packageDirectiveNode, importsList).forEach { astNode ->
            astNode?.treeNext?.apply {
                if (elementType == WHITE_SPACE && text.count { it == '\n' } != 2) {
                    FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(), astNode.startOffset) {
                        (this as LeafPsiElement).replaceWithText("\n\n${text.replace("\n", "")}")
                    }
                }
            }
        }
    }

    private fun checkImportsOrder(node: ASTNode) {
        val imports = node.getChildren(TokenSet.create(IMPORT_DIRECTIVE)).toList()

        // importPath can be null if import name cannot be parsed, which should be a very rare case, therefore !! should be safe here
        imports.filter { (it.psi as KtImportDirective).importPath!!.isAllUnder }.forEach {
            FILE_WILDCARD_IMPORTS.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset)
        }

        val sortedImports = imports.sortedBy { it.text }
        if (sortedImports != imports) {
            FILE_UNORDERED_IMPORTS.warnAndFix(configRules, emitWarn, isFixMode, fileName, node.startOffset) {
                rearrangeImports(node, imports, sortedImports)
            }
        }
    }

    private fun rearrangeImports(node: ASTNode, imports: List<ASTNode>, sortedImports: List<ASTNode>) {
        // move all commented lines among import before imports block
        node.getChildren(null).filterIndexed { index, astNode ->
            index < node.getChildren(null).indexOf(imports.last()) && astNode.elementType == EOL_COMMENT
        }.forEach {
            node.treeParent.addChild(it.clone() as ASTNode, node)
            node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node)
        }

        node.removeRange(imports.first(), imports.last())
        sortedImports.forEachIndexed { index, importNode ->
            if (index != 0) {
                node.addChild(PsiWhiteSpaceImpl("\n"), null)
            }
            node.addChild(importNode, null)
        }
    }

    @VisibleForTesting
    private fun ASTNode.getSiblingBlocks(copyrightComment: ASTNode?,
                                         headerKdoc: ASTNode?,
                                         fileAnnotations: ASTNode?,
                                         packageDirectiveNode: ASTNode
    ): Pair<ASTNode?, ASTNode> = when (elementType) {
        BLOCK_COMMENT -> null to listOfNotNull(headerKdoc, fileAnnotations, packageDirectiveNode).first()
        KDOC -> copyrightComment to (fileAnnotations ?: packageDirectiveNode)
        FILE_ANNOTATION_LIST -> (headerKdoc ?: copyrightComment) to packageDirectiveNode
        else -> null to packageDirectiveNode
    }

    private fun ASTNode.moveChildBefore(child: ASTNode, beforeThis: ASTNode, nBlankLinesAfter: Int = 0) {
        addChild(child.clone() as ASTNode, beforeThis)
        if (nBlankLinesAfter > 0) addChild(PsiWhiteSpaceImpl("\n".repeat(nBlankLinesAfter)), beforeThis)
        if (nBlankLinesAfter > 0) child.nextSibling { it.elementType == WHITE_SPACE }?.let { removeChild(it) }
        removeChild(child)
    }

    // fixme recommended order (see Recommendation 3.1) of imports can be checked by custom comparator
    private val recommendedComparator = Comparator<ASTNode> { import1, import2 ->
        require(import1.elementType == IMPORT_DIRECTIVE && import2.elementType == IMPORT_DIRECTIVE) { "This comparator is for sorting imports" }
        val pathSegments1 = (import1.psi as KtImportDirective).importPath!!.fqName.pathSegments()
        val pathSegments2 = (import2.psi as KtImportDirective).importPath!!.fqName.pathSegments()

        return@Comparator 0
    }
}
