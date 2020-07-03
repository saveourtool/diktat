package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.isLeaf
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.utils.*
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
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

class PackageNaming : Rule("package-naming") {
    companion object {
        const val PACKAGE_SEPARATOR = "."
        const val PACKAGE_PATH_ANCHOR = "src"
        val LANGUAGE_DIR_NAMES = listOf("src", "main", "java", "kotlin")
        private val log = LoggerFactory.getLogger(PackageNaming::class.java)
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private lateinit var domainName: String

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        configRules = params.getDiktatConfigRules()
        isFixMode = autoCorrect
        emitWarn = emit

        val domainNameConfiguration = configRules.getRuleConfig(PACKAGE_NAME_MISSING)?.configuration
        if (domainNameConfiguration == null) {
            log.error("Not able to find an external configuration for domain name in the configuration of" +
                " ${PACKAGE_NAME_MISSING.name} check (is it missing in json config?)")
        }
        val configuration = PackageNamingConfiguration(domainNameConfiguration ?: mapOf())
        domainName = configuration.domainName

        if (node.elementType == PACKAGE_DIRECTIVE) {
            // calculating package name based on the directory where the file is placed
            val realPackageName = calculateRealPackageName(params)

            // if node isLeaf - this means that there is no package name declared
            if (node.isLeaf()) {
                checkMissingPackageName(node, realPackageName, params.fileName!!)
                return
            }

            // getting all identifiers from existing package name into the list like [org, diktat, project]
            val wordsInPackageName = mutableListOf<ASTNode>()
            node.getAllLLeafsWithSpecificType(IDENTIFIER, wordsInPackageName)

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
        PACKAGE_NAME_MISSING.warnAndFix(configRules, emitWarn, isFixMode, fileName, packageDirectiveNode.startOffset) {
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
    private fun calculateRealPackageName(params: KtLint.Params): List<String> {
        val filePathParts = params.fileName?.splitPathToDirs()

        return if (filePathParts == null || !filePathParts.contains(PACKAGE_PATH_ANCHOR)) {
            log.error("Not able to determine a path to a scanned file or src directory cannot be found in it's path." +
                " Will not be able to determine correct package name. It can happen due to missing <src> directory in the path")
            listOf()
        } else {
            // creating a real package name:
            // 1) getting a path after the base project directory (after "src" directory)
            // 2) removing src/main/kotlin/java/e.t.c dirs and removing file name
            // 3) adding company's domain name at the beginning
            val fileSubDir = filePathParts.subList(filePathParts.lastIndexOf(PACKAGE_PATH_ANCHOR), filePathParts.size - 1)
                .filter { !LANGUAGE_DIR_NAMES.contains(it) }
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
                PACKAGE_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, it.text, it.startOffset) {
                    it.toLower()
                }
            }

        // package name should start from a company's domain name
        if (!isDomainMatches(wordsInPackageName)) {
            PACKAGE_NAME_INCORRECT_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, domainName, wordsInPackageName[0].startOffset) {
                val parentNodeToInsert = wordsInPackageName[0].parent(DOT_QUALIFIED_EXPRESSION)!!
                createAndInsertPackageName(parentNodeToInsert, wordsInPackageName[0].treeParent, domainName.split(PACKAGE_SEPARATOR))
            }
        }

        // all words should contain only ASCII letters or digits
        wordsInPackageName
            .filter { word -> !correctSymbolsAreUsed(word.text) }
            .forEach { PACKAGE_NAME_INCORRECT_SYMBOLS.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset) }

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
            INCORRECT_PACKAGE_SEPARATOR.warnAndFix(configRules, emitWarn, isFixMode, word.text, word.startOffset) {
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
        var compositeElementWithNameAndDot: CompositeElement? = null
        var childDot: LeafPsiElement? = null
        var childPackageNamePart: LeafPsiElement?

        packageNameToInsert.forEach { name ->
            // creating Composite object = ((IDENTIFIER) + (DOT))
            compositeElementWithNameAndDot = CompositeElement(REFERENCE_EXPRESSION)
            childDot = LeafPsiElement(DOT, PACKAGE_SEPARATOR)
            childPackageNamePart = LeafPsiElement(IDENTIFIER, name)

            // putting composite node first in the parent tree and adding IDENTIFIER and DOT as children to it after
            parentNode.addChild(compositeElementWithNameAndDot!!, insertBeforeNode)
            compositeElementWithNameAndDot!!.addChild(childPackageNamePart!!)
            compositeElementWithNameAndDot!!.addChild(childDot!!)
        }

        // removing extra DOT that is not needed if we were inserting the whole package name (not just a part) to the parent
        if (insertBeforeNode == null) {
            compositeElementWithNameAndDot!!.removeChild(childDot!!)
        }
    }

    /**
     * checking and fixing package directive if it does not match with the directory where the file is stored
     */
    private fun checkFilePathMatchesWithPackageName(packageNameParts: List<ASTNode>, realName: List<String>) {
        if (realName.isNotEmpty() && packageNameParts.map { node -> node.text } != realName) {
            PACKAGE_NAME_INCORRECT_PATH.warnAndFix(configRules, emitWarn, isFixMode, realName.joinToString(PACKAGE_SEPARATOR), packageNameParts[0].startOffset) {
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

    /**
     * this class represents json-map configuration with the only one field (domainName) - can be used for json parsing
     */
    class PackageNamingConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val domainName by config
    }
}
