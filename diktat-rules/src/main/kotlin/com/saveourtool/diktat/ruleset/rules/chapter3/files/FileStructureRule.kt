package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_CONTAINS_ONLY_COMMENTS
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_INCORRECT_BLOCKS_ORDER
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_UNORDERED_IMPORTS
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_WILDCARD_IMPORTS
import com.saveourtool.diktat.ruleset.constants.Warnings.UNUSED_IMPORT
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.rules.chapter1.PackageNaming.Companion.PACKAGE_SEPARATOR
import com.saveourtool.diktat.ruleset.utils.StandardPlatforms
import com.saveourtool.diktat.ruleset.utils.copyrightWords
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.handleIncorrectOrder
import com.saveourtool.diktat.ruleset.utils.ignoreImports
import com.saveourtool.diktat.ruleset.utils.isPartOf
import com.saveourtool.diktat.ruleset.utils.isPartOfComment
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.moveChildBefore
import com.saveourtool.diktat.ruleset.utils.nextSibling
import com.saveourtool.diktat.ruleset.utils.operatorMap
import com.saveourtool.diktat.ruleset.utils.prevSibling

import org.jetbrains.kotlin.KtNodeTypes.FILE_ANNOTATION_LIST
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_DIRECTIVE
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_LIST
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.PACKAGE_DIRECTIVE
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * Visitor for checking internal file structure.
 * 1. Checks file contains not only comments
 * 2. Ensures the following blocks order: Copyright, Header Kdoc, @file annotation, package name, Import statements,
 *    top class header and top function header comments, top-level classes or top-level functions
 * 3. Ensures there is a blank line between these blocks
 * 4. Ensures imports are ordered alphabetically without blank lines
 * 5. Ensures there are no wildcard imports
 */
class FileStructureRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(FILE_CONTAINS_ONLY_COMMENTS, FILE_INCORRECT_BLOCKS_ORDER, FILE_NO_BLANK_LINE_BETWEEN_BLOCKS,
        FILE_UNORDERED_IMPORTS, FILE_WILDCARD_IMPORTS, UNUSED_IMPORT),
) {
    private val domainName by lazy {
        configRules
            .getCommonConfiguration()
            .domainName
    }
    private val standardImportsAsName = StandardPlatforms
        .values()
        .associate { it to it.packages }
        .mapValues { (_, value) ->
            value.map { it.split(PACKAGE_SEPARATOR).map(Name::identifier) }
        }
    private var packageName = ""

    /**
     * There are groups of methods, which should be excluded from usage check without type resolution.
     * `componentN` is a method for N-th component in destructuring declarations.
     */
    private val ignoreImportsPatterns = setOf("component\\d+".toRegex())

    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            val wildcardImportsConfig = WildCardImportsConfig(
                this.configRules.getRuleConfig(FILE_WILDCARD_IMPORTS)?.configuration ?: emptyMap()
            )
            val importsGroupingConfig = ImportsGroupingConfig(
                this.configRules.getRuleConfig(FILE_UNORDERED_IMPORTS)?.configuration ?: emptyMap()
            )
            checkUnusedImport(node)
            node.findChildByType(IMPORT_LIST)
                ?.let { checkImportsOrder(it, wildcardImportsConfig, importsGroupingConfig) }
            if (checkFileHasCode(node)) {
                checkCodeBlocksOrderAndEmptyLines(node)
            }
            return
        }
    }

    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun checkFileHasCode(node: ASTNode): Boolean {
        val codeTokens = TokenSet.andNot(
            TokenSet.ANY,
            TokenSet.create(WHITE_SPACE, KDOC, BLOCK_COMMENT, EOL_COMMENT, PACKAGE_DIRECTIVE, IMPORT_LIST)
        )
        val hasCode = node.getChildren(codeTokens).isNotEmpty()
        if (!hasCode) {
            val freeText = if (node.text.isEmpty()) "file is empty" else "file contains no code"
            FILE_CONTAINS_ONLY_COMMENTS.warn(configRules, emitWarn, freeText, node.startOffset, node)
        }
        return hasCode
    }

    @Suppress(
        "ComplexMethod",
        "TOO_LONG_FUNCTION",
        "SpreadOperator"
    )
    private fun checkCodeBlocksOrderAndEmptyLines(node: ASTNode) {
        // From KtFile.kt: 'scripts have no package directive, all other files must have package directives'.
        // Kotlin compiler itself enforces it's position in the file if it is present.
        // If package directive is missing in .kt file (default package), the node is still present in the AST.
        val packageDirectiveNode = (node.psi as KtFile)
            .packageDirective
            ?.takeUnless { it.isRoot }
            ?.node
        // There is a private property node.psi.importLists, but it's size can't be > 1 in valid kotlin code. It exists to help in situations
        // when, e.g. merge conflict marker breaks the imports list. We shouldn't handle this situation here.
        val importsList = (node.psi as KtFile)
            .importList
            ?.takeIf { it.imports.isNotEmpty() }
            ?.node

        // this node will be an anchor with respect to which we will look for all other nodes
        val firstCodeNode = packageDirectiveNode
            ?: importsList
            ?: node.children().firstOrNull {
                // taking nodes with actual code
                !it.isWhiteSpace() && !it.isPartOfComment() &&
                        // but not the ones we are going to move
                        it.elementType != FILE_ANNOTATION_LIST &&
                        // if we are here, then IMPORT_LIST either is not present in the AST, or is empty. Either way, we don't need to select it.
                        it.elementType != IMPORT_LIST &&
                        // if we are here, then package is default and we don't need to select the empty PACKAGE_DIRECTIVE node.
                        it.elementType != PACKAGE_DIRECTIVE
            }
            ?: return  // at this point it means the file contains only comments
        // We consider the first block comment of the file to be the one that possibly contains copyright information.
        var copyrightComment = firstCodeNode.prevSibling { it.elementType == BLOCK_COMMENT }
            ?.takeIf { blockCommentNode ->
                copyrightWords.any { blockCommentNode.text.contains(it, ignoreCase = true) }
            }
        // firstCodeNode could be:
        // * package directive - in this case we looking for kdoc before package directive
        // and if it doesn't exist, additionally looking for kdoc before imports list
        // * imports list or actual code - if there is no kdoc before it, suppose that it is absent in file
        var headerKdoc = firstCodeNode.prevSibling { it.elementType == KDocTokens.KDOC }
            ?: if (firstCodeNode == packageDirectiveNode) importsList?.prevSibling { it.elementType == KDocTokens.KDOC } else null
        // Annotations with target`file` can only be placed before `package` directive.
        var fileAnnotations = node.findChildByType(FILE_ANNOTATION_LIST)
        // We also collect all other elements that are placed on top of the file.
        // These may be other comments, so we just place them before the code starts.
        val otherNodesBeforeCode = firstCodeNode.siblings(forward = false)
            .filterNot {
                it.isWhiteSpace() ||
                        it == copyrightComment || it == headerKdoc || it == fileAnnotations ||
                        it.elementType == PACKAGE_DIRECTIVE
            }
            .toList()
            .reversed()

        // checking order
        listOfNotNull(copyrightComment, headerKdoc, fileAnnotations, *otherNodesBeforeCode.toTypedArray()).handleIncorrectOrder({
            getSiblingBlocks(copyrightComment, headerKdoc, fileAnnotations, firstCodeNode, otherNodesBeforeCode)
        }) { astNode, beforeThisNode ->
            FILE_INCORRECT_BLOCKS_ORDER.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(), astNode.startOffset, astNode) {
                val result = node.moveChildBefore(astNode, beforeThisNode, true)
                result.newNodes.first().run {
                    // reassign values to the nodes that could have been moved
                    when (elementType) {
                        BLOCK_COMMENT -> copyrightComment = this
                        KDOC -> headerKdoc = this
                        FILE_ANNOTATION_LIST -> fileAnnotations = this
                    }
                }
                astNode.treeNext?.let { node.replaceChild(it, PsiWhiteSpaceImpl("\n\n")) }
            }
        }

        // checking empty lines
        insertNewlinesBetweenBlocks(listOf(copyrightComment, headerKdoc, fileAnnotations, packageDirectiveNode, importsList))
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkImportsOrder(
        node: ASTNode,
        wildCardImportsConfig: WildCardImportsConfig,
        importsGroupingConfig: ImportsGroupingConfig
    ) {
        val imports = node.getChildren(TokenSet.create(IMPORT_DIRECTIVE)).toList()
        // importPath can be null if import name cannot be parsed, which should be a very rare case, therefore !! should be safe here
        @Suppress("PARAMETER_NAME_IN_OUTER_LAMBDA")
        imports
            .filter {
                (it.psi as KtImportDirective).importPath!!.run {
                    isAllUnder && toString() !in wildCardImportsConfig.allowedWildcards
                }
            }
            .forEach { FILE_WILDCARD_IMPORTS.warn(configRules, emitWarn, it.text, it.startOffset, it) }
        val sortedImportsGroups = if (importsGroupingConfig.useRecommendedImportsOrder) {
            regroupImports(imports.map { it.psi as KtImportDirective })
                .map { group -> group.map { it.node } }
        } else {
            listOf(imports)
        }
            .map { group -> group.sortedBy { it.text } }
        if (sortedImportsGroups.flatten() != imports) {
            FILE_UNORDERED_IMPORTS.warnAndFix(configRules, emitWarn, isFixMode, "${sortedImportsGroups.flatten().first().text}...", node.startOffset, node) {
                rearrangeImports(node, imports, sortedImportsGroups)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkUnusedImport(
        node: ASTNode
    ) {
        val refSet = findAllReferences(node)
        packageName = (node.findChildByType(PACKAGE_DIRECTIVE)?.psi as KtPackageDirective).qualifiedName
        node.findChildByType(IMPORT_LIST)
            ?.getChildren(TokenSet.create(IMPORT_DIRECTIVE))
            ?.toList()
            ?.forEach { import ->
                val ktImportDirective = import.psi as KtImportDirective
                val importName = ktImportDirective.importPath?.importedName?.asString()
                val importPath = ktImportDirective.importPath?.pathStr!!  // importPath - ifNOtParsed & Nullable
                if (ktImportDirective.aliasName == null &&
                        packageName.isNotEmpty() && importPath.startsWith("$packageName.") &&
                        importPath.substring(packageName.length + 1).indexOf('.') == -1
                ) {
                    // this branch corresponds to imports from the same package
                    deleteImport(importPath, node, ktImportDirective)
                } else if (importName != null && !refSet.contains(importName)) {
                    // Fixme: operatorMap imports and `getValue` should be deleted if unused, but we can't detect for sure
                    val shouldImportBeIgnored = ignoreImports.contains(importName) || ignoreImportsPatterns.any { it.matches(importName) }
                    if (!shouldImportBeIgnored) {
                        // this import is not used anywhere
                        deleteImport(importPath, node, ktImportDirective)
                    }
                }
            }
    }

    private fun deleteImport(
        importPath: String,
        node: ASTNode,
        ktImportDirective: KtImportDirective
    ) {
        UNUSED_IMPORT.warnAndFix(
            configRules, emitWarn, isFixMode,
            "$importPath - unused import",
            node.startOffset, node
        ) { ktImportDirective.delete() }
    }

    private fun findAllReferences(node: ASTNode): Set<String> {
        val referencesFromOperations = node.findAllDescendantsWithSpecificType(OPERATION_REFERENCE)
            .filterNot { it.isPartOf(IMPORT_DIRECTIVE) }
            .flatMap { ref ->
                val references = operatorMap.filterValues { ref.text in it }
                if (references.isNotEmpty()) {
                    references.keys
                } else {
                    // this is needed to check infix functions that relate to operation reference
                    setOf(ref.text)
                }
            }
        val referencesFromExpressions = node.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)
            .filterNot { it.isPartOf(IMPORT_DIRECTIVE) }
            .map {
                // the importedName method removes the quotes, but the node.text method does not
                it.text.replace("`", "")
            }
        val referencesFromKdocs = node.findAllDescendantsWithSpecificType(KDocTokens.KDOC)
            .flatMap { it.findAllDescendantsWithSpecificType(KDocTokens.MARKDOWN_LINK) }
            .map { it.text.removePrefix("[").removeSuffix("]") }
            .flatMap {
                if (it.contains(".")) {
                    // support cases with reference to method
                    listOf(it, it.substringBeforeLast("."))
                } else {
                    listOf(it)
                }
            }
        return (referencesFromOperations + referencesFromExpressions + referencesFromKdocs).toSet()
    }

    private fun rearrangeImports(
        node: ASTNode,
        imports: List<ASTNode>,
        sortedImportsGroups: List<List<ASTNode>>
    ) {
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

    private fun insertNewlinesBetweenBlocks(blocks: List<ASTNode?>) {
        blocks.forEach { astNode ->
            // if package directive is missing, node is still present, but it's text is empty, so we need to check treeNext to get meaningful results
            astNode?.nextSibling { it.text.isNotEmpty() }?.apply {
                if (elementType == WHITE_SPACE && text.count { it == '\n' } != 2) {
                    FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(),
                        astNode.startOffset, astNode) {
                        (this as LeafPsiElement).rawReplaceWithText("\n\n${text.replace("\n", "")}")
                    }
                }
            }
        }
    }

    /**
     * @return a pair of nodes between which [this] node should be placed, i.e. after the first and before the second element
     */
    private fun ASTNode.getSiblingBlocks(
        copyrightComment: ASTNode?,
        headerKdoc: ASTNode?,
        fileAnnotations: ASTNode?,
        firstCodeNode: ASTNode,
        otherNodesBeforeFirst: List<ASTNode>
    ): Pair<ASTNode?, ASTNode> = when (this) {
        copyrightComment -> null to listOfNotNull(headerKdoc, fileAnnotations, otherNodesBeforeFirst.firstOrNull(), firstCodeNode).first()
        headerKdoc -> copyrightComment to (fileAnnotations ?: otherNodesBeforeFirst.firstOrNull() ?: firstCodeNode)
        fileAnnotations -> (headerKdoc ?: copyrightComment) to (otherNodesBeforeFirst.firstOrNull() ?: firstCodeNode)
        else -> (headerKdoc ?: copyrightComment) to firstCodeNode
    }

    @Suppress("TYPE_ALIAS", "UnsafeCallOnNullableType")
    private fun regroupImports(imports: List<KtImportDirective>): List<List<KtImportDirective>> {
        val (android, notAndroid) = imports.partition {
            it.isStandard(StandardPlatforms.ANDROID)
        }

        val (ownDomain, tmp) = domainName?.let { domainName ->
            notAndroid.partition { import ->
                import
                    .importPath
                    ?.fqName
                    ?.pathSegments()
                    ?.zip(domainName.split(PACKAGE_SEPARATOR).map(Name::identifier))
                    ?.all { it.first == it.second }
                    ?: false
            }
        } ?: Pair(emptyList(), notAndroid)

        val (others, javaAndKotlin) = tmp.partition {
            !it.isStandard(StandardPlatforms.JAVA) && !it.isStandard(StandardPlatforms.KOTLIN)
        }

        val (java, kotlin) = javaAndKotlin.partition { it.isStandard(StandardPlatforms.JAVA) }

        return listOf(android, ownDomain, others, java, kotlin)
    }

    private fun KtImportDirective.isStandard(platformName: StandardPlatforms) = standardImportsAsName[platformName]?.any { names ->
        names.zip(importPath?.fqName?.pathSegments() ?: emptyList())
            .all { it.first == it.second }
    } ?: false

    /**
     * [RuleConfiguration] for wildcard imports
     */
    class WildCardImportsConfig(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * A list of imports that are allowed to use wildcards. Input is in a form "foo.bar.*,foo.baz.*".
         */
        val allowedWildcards = config["allowedWildcards"]?.split(",")?.map { it.trim() } ?: emptyList()
    }

    /**
     * [RuleConfiguration] for imports grouping according to the recommendation from diktat code style
     */
    class ImportsGroupingConfig(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Use imports grouping according to recommendation 3.1
         */
        val useRecommendedImportsOrder = config["useRecommendedImportsOrder"]?.toBoolean() ?: true
    }

    companion object {
        const val NAME_ID = "file-structure"
    }
}
