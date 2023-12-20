package com.saveourtool.diktat.ruleset.rules.chapter1

import com.saveourtool.diktat.common.config.rules.CommonConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.ruleset.constants.Warnings.INCORRECT_PACKAGE_SEPARATOR
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_CASE
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PATH
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PREFIX
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_SYMBOLS
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_MISSING
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.util.isKotlinScript

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FILE_ANNOTATION_LIST
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_LIST
import org.jetbrains.kotlin.KtNodeTypes.PACKAGE_DIRECTIVE
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.PACKAGE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.psiUtil.children

import java.util.concurrent.atomic.AtomicInteger

/**
 * Rule 1.3: package name is in lower case and separated by dots, code developed internally in your company (in example Huawei) should start
 * with it's domain (like com.huawei), and the package name is allowed to have numbers
 *
 * Current limitations and FixMe:
 * need to support autofixing of directories in the same way as package is named. For example if we have package name:
 * package a.b.c.D -> then class D should be placed in a/b/c/ directories
 */
@Suppress("ForbiddenComment", "TOO_MANY_LINES_IN_LAMBDA")
class PackageNaming(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(INCORRECT_PACKAGE_SEPARATOR, PACKAGE_NAME_INCORRECT_CASE, PACKAGE_NAME_MISSING,
        PACKAGE_NAME_INCORRECT_PATH, PACKAGE_NAME_INCORRECT_PREFIX, PACKAGE_NAME_INCORRECT_SYMBOLS),
) {
    private lateinit var domainName: String

    override fun logic(node: ASTNode) {
        val configuration = configRules.getCommonConfiguration()
        configuration.domainName?.let {
            domainName = it
            if (node.elementType == PACKAGE_DIRECTIVE) {
                val filePath = node.getFilePath()

                // getting all identifiers from existing package name into the list like [org, diktat, project]
                val wordsInPackageName = node.findAllDescendantsWithSpecificType(IDENTIFIER)

                if (wordsInPackageName.isEmpty() && filePath.isKotlinScript()) {
                    // kotlin scripts are allowed to have empty package; in this case we don't suggest a new one and don't run checks
                    return
                }

                // calculating package name based on the directory where the file is placed
                val realPackageName = calculateRealPackageName(filePath, configuration)

                // if node isLeaf - this means that there is no package name declared
                if (node.isLeaf() && !filePath.isKotlinScript()) {
                    warnAndFixMissingPackageName(node, realPackageName, filePath)
                    return
                }

                // no need to check that packageIdentifiers is empty, because in this case parsing will fail
                checkPackageName(wordsInPackageName, node)
                // fix in checkFilePathMatchesWithPackageName is much more aggressive than fixes in checkPackageName, they can conflict
                checkFilePathMatchesWithPackageName(wordsInPackageName, realPackageName, node)
            }
        } ?: if (visitorCounter.incrementAndGet() == 1) {
            log.error {
                "Not able to find an external configuration for domain" +
                        " name in the common configuration (is it missing in yml config?)"
            }
        } else {
            @Suppress("RedundantUnitExpression")
            Unit
        }
    }

    /**
     * checking and fixing the case when package directive is missing in the file
     */
    private fun warnAndFixMissingPackageName(
        initialPackageDirectiveNode: ASTNode,
        realPackageName: List<String>,
        filePath: String
    ) {
        val fileName = filePath.substringAfterLast(File.separator)

        // if the file path contains "buildSrc" - don't add the package name to the file
        val isBuildSrcPath = "buildSrc" in filePath

        if (!isBuildSrcPath) {
            PACKAGE_NAME_MISSING.warnAndFix(configRules, emitWarn, isFixMode, fileName,
                initialPackageDirectiveNode.startOffset, initialPackageDirectiveNode) {
                if (realPackageName.isNotEmpty()) {
                    // creating node for package directive using Kotlin parser
                    val newPackageDirectiveName = realPackageName.joinToString(PACKAGE_SEPARATOR)
                    insertNewPackageName(initialPackageDirectiveNode, newPackageDirectiveName)
                }
            }
        }
    }

    /**
     * calculating real package name based on the directory path where the file is stored
     *
     * @return list with words that are parts of package name like [org, diktat, name]
     */
    private fun calculateRealPackageName(fileName: String, configuration: CommonConfiguration): List<String> {
        val filePathParts = fileName
            .splitPathToDirs()
            .dropLast(1)  // remove filename
            .flatMap { it.split(".") }

        return if (!filePathParts.contains(PACKAGE_PATH_ANCHOR)) {
            log.error {
                "Not able to determine a path to a scanned file or \"$PACKAGE_PATH_ANCHOR\" directory cannot be found in it's path." +
                        " Will not be able to determine correct package name. It can happen due to missing <$PACKAGE_PATH_ANCHOR> directory in the path"
            }
            emptyList()
        } else {
            // creating a real package name:
            // 1) getting a path after the base project directory (after "src" directory)
            // 2) removing src/main/kotlin/java/e.t.c dirs
            // 3) adding company's domain name at the beginning
            val allDirs = languageDirNames + configuration.srcDirectories + configuration.testAnchors
            val fileSubDir = filePathParts.subList(filePathParts.lastIndexOf(PACKAGE_PATH_ANCHOR), filePathParts.size)
                .dropWhile { allDirs.contains(it) }
            // no need to add DOMAIN_NAME to the package name if it is already in path
            val domainPrefix = if (!fileSubDir.joinToString(PACKAGE_SEPARATOR).startsWith(domainName)) domainName.split(PACKAGE_SEPARATOR) else emptyList()
            domainPrefix + fileSubDir
        }
    }

    private fun checkPackageName(wordsInPackageName: List<ASTNode>, packageDirectiveNode: ASTNode) {
        // all words should be in a lower case (lower case letters/digits/underscore)
        wordsInPackageName
            .filter { word -> word.text.hasUppercaseLetter() }
            .forEach { word ->
                PACKAGE_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, word.text, word.startOffset, word) {
                    word.toLower()
                }
            }

        // package name should start from a company's domain name
        if (!isDomainMatches(wordsInPackageName)) {
            PACKAGE_NAME_INCORRECT_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, domainName,
                wordsInPackageName[0].startOffset, wordsInPackageName[0]) {
                val oldPackageName = wordsInPackageName.joinToString(PACKAGE_SEPARATOR) { it.text }
                val newPackageName = "$domainName$PACKAGE_SEPARATOR$oldPackageName"
                insertNewPackageName(packageDirectiveNode, newPackageName)
            }
        }

        // all words should contain only ASCII letters or digits
        wordsInPackageName
            .filter { word -> !areCorrectSymbolsUsed(word.text) }
            .forEach { PACKAGE_NAME_INCORRECT_SYMBOLS.warn(configRules, emitWarn, it.text, it.startOffset, it) }

        // all words should contain only ASCII letters or digits
        wordsInPackageName.forEach { correctPackageWordSeparatorsUsed(it) }
    }

    /**
     * only letters, digits and underscore are allowed
     */
    private fun areCorrectSymbolsUsed(word: String): Boolean {
        // underscores are allowed in some cases - see "exceptionForUnderscore"
        val wordFromPackage = word.replace("_", "")
        return wordFromPackage.isASCIILettersAndDigits()
    }

    /**
     * in package name no other separators except dot should be used, package words (parts) should be concatenated
     * without any symbols or should use dot symbol - this is the only way
     */
    private fun correctPackageWordSeparatorsUsed(word: ASTNode) {
        if (word.text.contains("_") && !isExceptionForUnderscore(word.text)) {
            INCORRECT_PACKAGE_SEPARATOR.warnAndFix(configRules, emitWarn, isFixMode, word.text, word.startOffset, word) {
                (word as LeafPsiElement).rawReplaceWithText(word.text.replace("_", ""))
            }
        }
    }

    /** Underscores! In some cases, if the package name starts with a number or other characters,
     * but these characters cannot be used at the beginning of the Java/Kotlin package name,
     * or the package name contains reserved Java keywords, underscores are allowed.
     * For example: org.example.hyphenated_name,int_.example, com.example._123name
     */
    private fun isExceptionForUnderscore(word: String): Boolean {
        val wordFromPackage = word.replace("_", "")

        return wordFromPackage[0].isDigit() ||
                wordFromPackage.isKotlinKeyWord() ||
                wordFromPackage.isJavaKeyWord()
    }

    /**
     * function simply checks that package name starts with a proper domain name
     */
    private fun isDomainMatches(packageNameParts: List<ASTNode>): Boolean {
        val packageNamePrefix = domainName.split(PACKAGE_SEPARATOR)
        if (packageNameParts.size < packageNamePrefix.size) {
            return false
        }

        for (i in packageNamePrefix.indices) {
            if (packageNameParts[i].text != packageNamePrefix[i]) {
                return false
            }
        }
        return true
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun insertNewPackageName(packageDirectiveNode: ASTNode, packageName: String) {
        // package name can be dot qualified expression or a reference expression in case it contains only one word
        val packageNameNode = packageDirectiveNode.findChildByType(DOT_QUALIFIED_EXPRESSION)
            ?: packageDirectiveNode.findChildByType(REFERENCE_EXPRESSION)

        val generatedPackageDirective = KotlinParser()
            .createNode("$PACKAGE_KEYWORD $packageName", true)

        packageNameNode?.let {
            // simply replacing only node connected with the package name, all other nodes remain unchanged
            packageDirectiveNode.replaceChild(packageNameNode,
                generatedPackageDirective.findLeafWithSpecificType(DOT_QUALIFIED_EXPRESSION)!!)
        }
            ?: run {
                // there is missing package statement in a file, so it will be created and inserted
                val newPackageDirective = generatedPackageDirective.findLeafWithSpecificType(PACKAGE_DIRECTIVE)!!
                val packageDirectiveParent = packageDirectiveNode.treeParent
                // When package directive is missing in .kt file,
                // the node is still present in the AST, and not always in a convenient place.
                // E.g. `@file:Suppress("...") // comments`
                // AST will be: FILE_ANNOTATION_LIST, PACKAGE_DIRECTIVE, WHITE_SPACE, EOL_COMMENT
                // So, we can't just put new package directive in it's old place and rely on FileStructure rule
                if (packageDirectiveNode != packageDirectiveParent.firstChildNode) {
                    // We will insert new package directive node before first node, which is not in the following list
                    val possibleTypesBeforePackageDirective = listOf(WHITE_SPACE, EOL_COMMENT, BLOCK_COMMENT, KDOC, PACKAGE_DIRECTIVE, FILE_ANNOTATION_LIST)
                    val addBefore = packageDirectiveParent.children().first { it.elementType !in possibleTypesBeforePackageDirective }
                    packageDirectiveParent.removeChild(packageDirectiveNode)
                    packageDirectiveParent.addChild(newPackageDirective, addBefore)
                    if (newPackageDirective.treePrev.elementType != WHITE_SPACE) {
                        packageDirectiveParent.addChild(PsiWhiteSpaceImpl("\n"), newPackageDirective)
                    }
                } else {
                    packageDirectiveParent.replaceChild(packageDirectiveNode, newPackageDirective)
                }
                addWhiteSpaceIfRequired(newPackageDirective, packageDirectiveParent)
            }
    }

    private fun addWhiteSpaceIfRequired(packageNode: ASTNode, packageParentNode: ASTNode) {
        if (packageNode.treeNext.isWhiteSpace()) {
            return
        }
        if (!packageNode.treeNext.isEmptyImportList()) {
            packageParentNode.addChild(ASTFactory.whitespace("\n"), packageNode.treeNext)
        } else {
            // IMPORT_LIST without imports is after PACKAGE_NODE
            // WHITE_SPACE needs to be after IMPORT_LIST only
            packageParentNode.addChild(ASTFactory.whitespace("\n"), packageNode.treeNext.treeNext)
        }
    }

    /**
     * checking and fixing package directive if it does not match with the directory where the file is stored
     */
    private fun checkFilePathMatchesWithPackageName(packageNameParts: List<ASTNode>,
                                                    realNameParts: List<String>,
                                                    packageDirective: ASTNode
    ) {
        if (realNameParts.isNotEmpty() && packageNameParts.map { node -> node.text } != realNameParts) {
            val realPackageNameStr = realNameParts.joinToString(PACKAGE_SEPARATOR)
            val offset = packageNameParts[0].startOffset
            PACKAGE_NAME_INCORRECT_PATH.warnAndFix(configRules, emitWarn, isFixMode,
                realPackageNameStr, offset, packageNameParts[0]) {
                insertNewPackageName(packageDirective, realPackageNameStr)
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        const val NAME_ID = "package-naming"

        /**
         * Directory which is considered the start of sources file tree
         */
        const val PACKAGE_PATH_ANCHOR = SRC_DIRECTORY_NAME

        /**
         * Symbol that is used to separate parts in package name
         */
        const val PACKAGE_SEPARATOR = "."

        /**
         * tricky hack (counter) that helps not to raise multiple warnings about the package name if config is missing
         */
        var visitorCounter = AtomicInteger(0)

        /**
         * Targets described in [KMM documentation](https://kotlinlang.org/docs/reference/mpp-supported-platforms.html)
         */
        private val kmmTargets = listOf("common", "jvm", "js", "android", "ios", "androidNativeArm32", "androidNativeArm64", "iosArm32", "iosArm64", "iosX64",
            "watchosArm32", "watchosArm64", "watchosX86", "tvosArm64", "tvosX64", "macosX64", "linuxArm64", "linuxArm32Hfp", "linuxMips32", "linuxMipsel32", "linuxX64",
            "mingwX64", "mingwX86", "wasm32", "macosArm64")

        /**
         * Directories that are supposed to be first in sources file paths, relative to [PACKAGE_PATH_ANCHOR].
         * For kotlin multiplatform projects directories for targets from [kmmTargets] are supported.
         */
        val languageDirNames = listOf("src", "java", "kotlin") + kmmTargets.flatMap { listOf("${it}Main", "${it}Test") }

        private fun ASTNode.isEmptyImportList() = elementType == IMPORT_LIST && children().none()
    }
}
