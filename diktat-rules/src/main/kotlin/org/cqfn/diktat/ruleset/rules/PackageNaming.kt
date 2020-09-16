package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings.INCORRECT_PACKAGE_SEPARATOR
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_CASE
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PATH
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_SYMBOLS
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_MISSING
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFileName
import org.cqfn.diktat.ruleset.utils.getRootNode
import org.cqfn.diktat.ruleset.utils.hasUppercaseLetter
import org.cqfn.diktat.ruleset.utils.isASCIILettersAndDigits
import org.cqfn.diktat.ruleset.utils.isJavaKeyWord
import org.cqfn.diktat.ruleset.utils.isKotlinKeyWord
import org.cqfn.diktat.ruleset.utils.splitPathToDirs
import org.cqfn.diktat.ruleset.utils.toLower
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens.PACKAGE_KEYWORD
import org.slf4j.LoggerFactory

/**
 * Rule 1.3: package name is in lower case and separated by dots, code developed internally in your company (in example Huawei) should start
 * with it's domain (like com.huawei), and the package name is allowed to have numbers
 *
 * Current limitations and FixMe:
 * need to support autofixing of directories in the same way as package is named. For example if we have package name:
 * package a.b.c.D -> then class D should be placed in a/b/c/ directories
 */
@Suppress("ForbiddenComment")
class PackageNaming(private val configRules: List<RulesConfig>) : Rule("package-naming") {
    companion object {
        const val PACKAGE_SEPARATOR = "."
        const val PACKAGE_PATH_ANCHOR = "src"
        val LANGUAGE_DIR_NAMES = listOf("src", "main", "test", "java", "kotlin")
        private val log = LoggerFactory.getLogger(PackageNaming::class.java)
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private lateinit var domainName: String

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        val domainNameConfiguration = configRules.getCommonConfig()?.configuration
        if (domainNameConfiguration == null) {
            log.error("Not able to find an external configuration for domain name in the common configuration (is it missing in yml config?)")
        }
        val configuration = configRules.getCommonConfiguration().value
        domainName = configuration.domainName

        if (node.elementType == PACKAGE_DIRECTIVE) {
            val fileName = node.getRootNode().getFileName()
            // calculating package name based on the directory where the file is placed
            val realPackageName = calculateRealPackageName(fileName)

            // if node isLeaf - this means that there is no package name declared
            if (node.isLeaf()) {
                checkMissingPackageName(node, realPackageName, fileName)
                return
            }

            // getting all identifiers from existing package name into the list like [org, diktat, project]
            val wordsInPackageName = mutableListOf<ASTNode>()
            node.getAllLeafsWithSpecificType(IDENTIFIER, wordsInPackageName)

            // no need to check that packageIdentifiers is empty, because in this case parsing will fail
            checkPackageName(wordsInPackageName)
            // fix in checkFilePathMatchesWithPackageName is much more aggressive than fixes in checkPackageName, they can conflict
            checkFilePathMatchesWithPackageName(wordsInPackageName, realPackageName)
        }
    }

    /**
     * checking and fixing the case when package directive is missing in the file
     */
    private fun checkMissingPackageName(packageDirectiveNode: ASTNode, realPackageName: List<String>, fileName: String) {
        PACKAGE_NAME_MISSING.warnAndFix(configRules, emitWarn, isFixMode, fileName, packageDirectiveNode.startOffset, packageDirectiveNode) {
            if (realPackageName.isNotEmpty()) {
                packageDirectiveNode.addChild(LeafPsiElement(PACKAGE_KEYWORD, PACKAGE_KEYWORD.toString()), null)
                packageDirectiveNode.addChild(PsiWhiteSpaceImpl(" "), null)
                createAndInsertPackageName(packageDirectiveNode, null, realPackageName)
                packageDirectiveNode.addChild(PsiWhiteSpaceImpl("\n"), null)
                packageDirectiveNode.addChild(PsiWhiteSpaceImpl("\n"), null)
            }
        }
    }

    /**
     * calculating real package name based on the directory path where the file is stored
     * @return - list with words that are parts of package name like [org, diktat, name]
     */
    private fun calculateRealPackageName(fileName: String): List<String> {
        val filePathParts = fileName.splitPathToDirs()

        return if (!filePathParts.contains(PACKAGE_PATH_ANCHOR)) {
            log.error("Not able to determine a path to a scanned file or src directory cannot be found in it's path." +
                " Will not be able to determine correct package name. It can happen due to missing <src> directory in the path")
            listOf()
        } else {
            // creating a real package name:
            // 1) getting a path after the base project directory (after "src" directory)
            // 2) removing src/main/kotlin/java/e.t.c dirs and removing file name
            // 3) adding company's domain name at the beginning
            val fileSubDir = filePathParts.subList(filePathParts.lastIndexOf(PACKAGE_PATH_ANCHOR), filePathParts.size - 1)
                .dropWhile { LANGUAGE_DIR_NAMES.contains(it) }
            // no need to add DOMAIN_NAME to the package name if it is already in path
            val domainPrefix = if (!fileSubDir.joinToString(PACKAGE_SEPARATOR).startsWith(domainName)) domainName.split(PACKAGE_SEPARATOR) else listOf()
            domainPrefix + fileSubDir
        }
    }

    /**
     * FixMe: need to support auto correction of:
     * 1) directory should match with package name
     * 2) if package in incorrect case -> transform to lower
     */
    private fun checkPackageName(wordsInPackageName: List<ASTNode>) {
        // all words should be in a lower case (lower case letters/digits/underscore)
        wordsInPackageName
            .filter { word -> word.text.hasUppercaseLetter() }
            .forEach {
                PACKAGE_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, it.text, it.startOffset, it) {
                    it.toLower()
                }
            }

        // package name should start from a company's domain name
        if (!isDomainMatches(wordsInPackageName)) {
            PACKAGE_NAME_INCORRECT_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, domainName, wordsInPackageName[0].startOffset, wordsInPackageName[0]) {
                val parentNodeToInsert = wordsInPackageName[0].parent(DOT_QUALIFIED_EXPRESSION)!!
                createAndInsertPackageName(parentNodeToInsert, wordsInPackageName[0].treeParent, domainName.split(PACKAGE_SEPARATOR))
            }
        }

        // all words should contain only ASCII letters or digits
        wordsInPackageName
            .filter { word -> !correctSymbolsAreUsed(word.text) }
            .forEach { PACKAGE_NAME_INCORRECT_SYMBOLS.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset, it) }

        // all words should contain only ASCII letters or digits
        wordsInPackageName.forEach { correctPackageWordSeparatorsUsed(it) }
    }

    /**
     * only letters, digits and underscore are allowed
     */
    private fun correctSymbolsAreUsed(word: String): Boolean {
        // underscores are allowed in some cases - see "exceptionForUnderscore"
        val wordFromPackage = word.replace("_", "")
        return wordFromPackage.isASCIILettersAndDigits()
    }

    /**
     * in package name no other separators except dot should be used, package words (parts) should be concatenated
     * without any symbols or should use dot symbol - this is the only way
     */
    private fun correctPackageWordSeparatorsUsed(word: ASTNode) {
        if (word.text.contains("_") && !exceptionForUnderscore(word.text)) {
            INCORRECT_PACKAGE_SEPARATOR.warnAndFix(configRules, emitWarn, isFixMode, word.text, word.startOffset, word) {
                (word as LeafPsiElement).replaceWithText(word.text.replace("_", ""))
            }
        }
    }

    /** Underscores! In some cases, if the package name starts with a number or other characters,
     * but these characters cannot be used at the beginning of the Java/Kotlin package name,
     * or the package name contains reserved Java keywords, underscores are allowed.
     * For example: org.example.hyphenated_name,int_.example, com.example._123name
     */
    private fun exceptionForUnderscore(word: String): Boolean {
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
        if (packageNameParts.size < packageNamePrefix.size) return false

        for (i in packageNamePrefix.indices) {
            if (packageNameParts[i].text != packageNamePrefix[i]) {
                return false
            }
        }
        return true
    }

    /**
     * method for creating and inserting package name into the parentNode
     * Will create composite object = (Identifier + Dot) and insert it into the children list of parent node
     * FixMe: check if proper line/char numbers are added
     */
    private fun createAndInsertPackageName(parentNode: ASTNode, insertBeforeNode: ASTNode?, packageNameToInsert: List<String>) {
        packageNameToInsert.map { name ->
            // creating Composite object = ((IDENTIFIER) + (DOT))
            val compositeElementWithNameAndDot = CompositeElement(REFERENCE_EXPRESSION)
            val childDot = LeafPsiElement(DOT, PACKAGE_SEPARATOR)
            val childPackageNamePart = LeafPsiElement(IDENTIFIER, name)

            // putting composite node first in the parent tree and adding IDENTIFIER and DOT as children to it after
            parentNode.addChild(compositeElementWithNameAndDot, insertBeforeNode)
            compositeElementWithNameAndDot.addChild(childPackageNamePart)
            compositeElementWithNameAndDot.addChild(childDot)
            compositeElementWithNameAndDot
        }
                .lastOrNull()
                ?.let { compositeElementWithNameAndDot ->
                    // removing extra DOT that is not needed if we were inserting the whole package name (not just a part) to the parent
                    if (insertBeforeNode == null) {
                        compositeElementWithNameAndDot.removeChild(compositeElementWithNameAndDot.lastChildNode)
                    }
                }
    }

    /**
     * checking and fixing package directive if it does not match with the directory where the file is stored
     */
    private fun checkFilePathMatchesWithPackageName(packageNameParts: List<ASTNode>, realName: List<String>) {
        if (realName.isNotEmpty() && packageNameParts.map { node -> node.text } != realName) {
            PACKAGE_NAME_INCORRECT_PATH.warnAndFix(configRules, emitWarn, isFixMode, realName.joinToString(PACKAGE_SEPARATOR),
                    packageNameParts[0].startOffset, packageNameParts[0]) {
                // need to get first top-level DOT-QUALIFIED-EXPRESSION
                // -- PACKAGE_DIRECTIVE
                //    -- DOT_QUALIFIED_EXPRESSION
                val parentNode = packageNameParts[0]
                    .parent(PACKAGE_DIRECTIVE)!!
                    .findChildByType(DOT_QUALIFIED_EXPRESSION)!!

                parentNode.getChildren(null).forEach { node -> parentNode.removeChild(node) }
                createAndInsertPackageName(parentNode, null, realName)
            }
        }
    }
}
