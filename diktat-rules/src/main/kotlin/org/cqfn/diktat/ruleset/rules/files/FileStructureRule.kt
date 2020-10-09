package org.cqfn.diktat.ruleset.rules.files

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
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_CONTAINS_ONLY_COMMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_INCORRECT_BLOCKS_ORDER
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_UNORDERED_IMPORTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_WILDCARD_IMPORTS
import org.cqfn.diktat.ruleset.rules.PackageNaming.Companion.PACKAGE_SEPARATOR
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.getFileName
import org.cqfn.diktat.ruleset.utils.handleIncorrectOrder
import org.cqfn.diktat.ruleset.utils.moveChildBefore
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
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
class FileStructureRule(private val configRules: List<RulesConfig>) : Rule("file-structure") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private var fileName: String = ""
    private val domainName by lazy {
        configRules.getCommonConfiguration().value.domainName
    }

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == ElementType.FILE) {
            fileName = node.getFileName()
            val wildcardImportsConfig = WildCardImportsConfig(
                this.configRules.getRuleConfig(FILE_WILDCARD_IMPORTS)?.configuration ?: mapOf()
            )
            val importsGroupingConfig = ImportsGroupingConfig(
                this.configRules.getRuleConfig(FILE_UNORDERED_IMPORTS)?.configuration ?: mapOf()
            )
            node.findChildByType(IMPORT_LIST)
                ?.let { checkImportsOrder(it, wildcardImportsConfig, importsGroupingConfig) }
            if (checkFileHasCode(node)) {
                checkCodeBlocksOrderAndEmptyLines(node)
            }
        }
    }

    private fun checkFileHasCode(node: ASTNode): Boolean {
        val codeTokens = TokenSet.andNot(TokenSet.ANY, TokenSet.create(WHITE_SPACE, KDOC, BLOCK_COMMENT, EOL_COMMENT, PACKAGE_DIRECTIVE, IMPORT_LIST))
        val hasCode = node.getChildren(codeTokens).isNotEmpty()
        if (!hasCode) {
            FILE_CONTAINS_ONLY_COMMENTS.warn(configRules, emitWarn, isFixMode, fileName, node.startOffset, node)
        }
        return hasCode
    }

    private fun checkCodeBlocksOrderAndEmptyLines(node: ASTNode) {
        // fixme handle other elements that could be present before package (other comments)
        val copyrightComment = node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)
        val headerKdoc = node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)
        val fileAnnotations = node.findChildByType(FILE_ANNOTATION_LIST)
        // PACKAGE_DIRECTIVE node is always present in regular kt files and might be absent in kts
        // kotlin compiler itself enforces it's position in the file if it is present
        // fixme: handle cases when this node is not present
        val packageDirectiveNode = (node.psi as KtFile).packageDirective?.node ?: return
        // fixme: find cases when node.psi.importLists.size > 1, handle cases when it's not present
        val importsList = (node.psi as KtFile).importList?.node ?: return

        // checking order
        listOfNotNull(copyrightComment, headerKdoc, fileAnnotations).handleIncorrectOrder({
            getSiblingBlocks(copyrightComment, headerKdoc, fileAnnotations, packageDirectiveNode)
        }) { astNode, beforeThisNode ->
            FILE_INCORRECT_BLOCKS_ORDER.warnAndFix(
                configRules,
                emitWarn,
                isFixMode,
                astNode.text.lines().first(),
                astNode.startOffset,
                astNode
            ) {
                node.moveChildBefore(astNode, beforeThisNode, true)
                astNode.treeNext?.let { node.replaceChild(it, PsiWhiteSpaceImpl("\n\n")) }
            }
        }

        // checking empty lines
        arrayOf(copyrightComment, headerKdoc, fileAnnotations, packageDirectiveNode, importsList).forEach { astNode ->
            astNode?.treeNext?.apply {
                if (elementType == WHITE_SPACE && text.count { it == '\n' } != 2) {
                    FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(),
                        astNode.startOffset, astNode) {
                        (this as LeafPsiElement).replaceWithText("\n\n${text.replace("\n", "")}")
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkImportsOrder(
        node: ASTNode,
        wildCardImportsConfig: WildCardImportsConfig,
        importsGroupingConfig: ImportsGroupingConfig
    ) {
        val imports = node.getChildren(TokenSet.create(IMPORT_DIRECTIVE)).toList()

        // importPath can be null if import name cannot be parsed, which should be a very rare case, therefore !! should be safe here
        imports.filter { (it.psi as KtImportDirective).importPath!!.isAllUnder && it.text !in wildCardImportsConfig.allowedWildcards }
            .forEach {
                FILE_WILDCARD_IMPORTS.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset, it)
            }

        val sortedImportsGroups = if (importsGroupingConfig.useRecommendedImportsOrder) {
            ImportGroups.create(imports.map { it.psi as KtImportDirective }, domainName).toList()
                .map { group -> group.map { it.node } }
        } else {
            listOf(imports)
        }
            .map { group -> group.sortedBy { it.text } }

        if (sortedImportsGroups.flatten() != imports) {
            FILE_UNORDERED_IMPORTS.warnAndFix(configRules, emitWarn, isFixMode, fileName, node.startOffset, node) {
                rearrangeImports(node, imports, sortedImportsGroups)
            }
        }
    }

    private fun rearrangeImports(node: ASTNode, imports: List<ASTNode>, sortedImportsGroups: List<List<ASTNode>>) {
        require(node.elementType == IMPORT_LIST)
        // move all commented lines among import before imports block
        node.getChildren(TokenSet.create(EOL_COMMENT))
            .forEach {
                node.treeParent.addChild(it.clone() as ASTNode, node)
                node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node)
            }

        node.removeRange(imports.first(), imports.last())
        sortedImportsGroups.filterNot { it.isEmpty() }
            .run {
                forEachIndexed { groupIndex, group ->
                    group.forEachIndexed { index, importNode ->
                        node.addChild(importNode, null)
                        if (index != group.size - 1) {
                            node.addChild(PsiWhiteSpaceImpl("\n"), null)
                        }
                    }
                    if (groupIndex != size - 1) {
                        node.addChild(PsiWhiteSpaceImpl("\n\n"), null)
                    }
                }
            }
    }

    private fun ASTNode.getSiblingBlocks(
        copyrightComment: ASTNode?,
        headerKdoc: ASTNode?,
        fileAnnotations: ASTNode?,
        packageDirectiveNode: ASTNode
    ): Pair<ASTNode?, ASTNode> = when (elementType) {
        BLOCK_COMMENT -> null to listOfNotNull(headerKdoc, fileAnnotations, packageDirectiveNode).first()
        KDOC -> copyrightComment to (fileAnnotations ?: packageDirectiveNode)
        FILE_ANNOTATION_LIST -> (headerKdoc ?: copyrightComment) to packageDirectiveNode
        else -> error("Only BLOCK_COMMENT, KDOC and FILE_ANNOTATION_LIST are valid inputs.")
    }

    private data class ImportGroups(
        val android: List<KtImportDirective>,
        val ownDomain: List<KtImportDirective>,
        val others: List<KtImportDirective>,
        val javaCore: List<KtImportDirective>,
        val kotlinCore: List<KtImportDirective>
    ) {
        fun toList() = listOf(android, ownDomain, others, javaCore, kotlinCore)

        companion object {
            fun create(imports: List<KtImportDirective>, domainName: String): ImportGroups {
                val (android, notAndroid) = imports.partitionByImportParts {
                    it.isStandardAndroid()
                }

                val (ownDomain, tmp) = notAndroid.partitionByImportParts { paths ->
                    paths.map { it.identifier }.zip(domainName.split(PACKAGE_SEPARATOR)).all { it.first == it.second }
                }

                val (others, javaAndKotlin) = tmp.partitionByImportParts {
                    !it.isStandardJava() && !it.isStandardKotlin()
                }

                val (java, kotlin) = javaAndKotlin.partitionByImportParts { it.isStandardJava() }

                return ImportGroups(android, ownDomain, others, java, kotlin)
            }

            private inline fun List<KtImportDirective>.partitionByImportParts(predicate: (List<Name>) -> Boolean) =
                partition {
                    it.importPath!!.fqName.pathSegments().let(predicate)
                }

            private fun List<Name>.isStandardAndroid() =
                first().identifier.let { it == "android" || it == "androidx" } ||
                        first().identifier == "com" && getOrNull(1)?.identifier == "android"

            private fun List<Name>.isStandardJava() = first().identifier.let { it == "java" || it == "javax" }

            private fun List<Name>.isStandardKotlin() = first().identifier.let { it == "kotlin" || it == "kotlinx" }
        }
    }

    class WildCardImportsConfig(config: Map<String, String>) : RuleConfiguration(config) {
        val allowedWildcards = config["allowedWildcards"]?.split(",")?.map { it.trim() } ?: listOf()
    }

    class ImportsGroupingConfig(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Use imports grouping according to recommendation 3.1
         */
        val useRecommendedImportsOrder = config["useRecommendedImportsOrder"]?.toBoolean() ?: true
    }
}
