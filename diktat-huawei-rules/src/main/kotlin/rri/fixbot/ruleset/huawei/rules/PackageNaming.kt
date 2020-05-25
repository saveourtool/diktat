package rri.fixbot.ruleset.huawei.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.isLeaf
import config.rules.RulesConfig
import config.rules.isRuleEnabled
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.utils.*
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
        // FixMe: these constants should be moved to configuration file
        const val PACKAGE_SEPARATOR = "."
        const val DOMAIN_NAME = "com.huawei"
        const val PACKAGE_PATH_ANCHOR = "src"
        val LANGUAGE_DIR_NAMES = listOf("src", "main", "java", "kotlin")
        private val log = LoggerFactory.getLogger(PackageNaming::class.java)
    }

    private var confiRules: List<RulesConfig> = emptyList()


    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        confiRules = params.rulesConfigList!!

        if (node.elementType == ElementType.PACKAGE_DIRECTIVE) {
            val realPackageName = calculateRealPackageName(params)

            // if node isLeaf - this means that there is no package name declared
            if (node.isLeaf()) {
                if (confiRules.isRuleEnabled(PACKAGE_NAME_MISSING)) {
                    emit(node.startOffset, PACKAGE_NAME_MISSING.warnText, true)
                    if (autoCorrect) {
                        // FixMe: need to find proper constant in kotlin for "package" keyword
                        node.addChild(LeafPsiElement(ElementType.PACKAGE_KEYWORD, "package"), null)
                        node.addChild(PsiWhiteSpaceImpl(" "), null)
                        createAndInsertPackageName(node, null, realPackageName)
                        node.addChild(PsiWhiteSpaceImpl("\n"), null)
                    }
                }
                return
            }

            // getting all identifiers from package name into the list like [com, huawei, project]
            val wordsInPackageName = mutableListOf<ASTNode>()
            node.getAllLLeafsWithSpecificType(ElementType.IDENTIFIER, wordsInPackageName)

            // no need to check that packageIdentifiers is empty, because in this case parsing will fail
            checkPackageName(autoCorrect, wordsInPackageName, emit)
            // fix in checkFilePathMatchesWithPackageName is much more agressive than fixes in checkPackageName, they can conflict
            checkFilePathMatchesWithPackageName(wordsInPackageName, realPackageName, autoCorrect, emit)
        }
    }

    private fun calculateRealPackageName(params: KtLint.Params): List<String> {
        val filePathParts = params.fileName?.splitPathToDirs()

        return if (filePathParts == null || !filePathParts.contains(PACKAGE_PATH_ANCHOR)) {
            log.error("Not able to determine a path to a scanned file or src directory cannot be found in it's path." +
                " Will not be able to determine correct package name. ")
            listOf()
        } else {
            // creating a real package name:
            // 1) getting a path after the base project directory (after "src" directory)
            // 2) removing src/main/kotlin/java/e.t.c dirs and removing file name
            // 3) adding company's domain name at the beginning
            DOMAIN_NAME.split(PACKAGE_SEPARATOR) +
                filePathParts.subList(filePathParts.lastIndexOf(PACKAGE_PATH_ANCHOR), filePathParts.size - 1)
                    .filter { !LANGUAGE_DIR_NAMES.contains(it) }
        }
    }

    /**
     * FixMe: need to support auto correction of:
     * 1) directory should match with package name
     * 2) if package in incorrect case -> transform to lower
     */
    private fun checkPackageName(
        autoCorrect: Boolean,
        wordsInPackageName: List<ASTNode>,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        // all words should be in a lower case (lower case letters/digits/underscore)
        wordsInPackageName
            .filter { word -> hasUppercaseLetter(word.text) }
            .forEach {
                if (confiRules.isRuleEnabled(PACKAGE_NAME_INCORRECT_CASE)) {
                    emit(it.startOffset, "${PACKAGE_NAME_INCORRECT_CASE.warnText} ${it.text}", true)
                    if (autoCorrect) {
                        (it as LeafPsiElement).`replaceWithText`(it.text.toLowerCase())
                    }
                }
            }

        // package name should start from a company's domain name
        if (!isDomainMatches(wordsInPackageName)) {
            if (confiRules.isRuleEnabled(PACKAGE_NAME_INCORRECT_PREFIX)) {
                emit(wordsInPackageName[0].startOffset, "${PACKAGE_NAME_INCORRECT_PREFIX.warnText} $DOMAIN_NAME", true)
                if (autoCorrect) {
                    // FixMe: .treeParent.treeParent is called to get DOT_QUALIFIED_EXPRESSION - it can be done in more elegant way
                    val parentNodeToInsert = wordsInPackageName[0].treeParent.treeParent
                    createAndInsertPackageName(parentNodeToInsert, wordsInPackageName[0].treeParent, DOMAIN_NAME.split(PACKAGE_SEPARATOR))
                }
            }
        }

        // all words should contain only letters or digits
        wordsInPackageName.filter { word -> !correctSymbolsAreUsed(word.text) }.forEach {
            if (confiRules.isRuleEnabled(PACKAGE_NAME_INCORRECT_SYMBOLS)) {
                emit(it.startOffset, "${PACKAGE_NAME_INCORRECT_SYMBOLS.warnText} ${it.text}", true)
                if (autoCorrect) {
                    // FixMe: cover with tests
                    // FixMe: add different auto corrections for incorrect separators, letters, e.t.c
                }
            }
        }
    }

    private fun hasUppercaseLetter(word: String): Boolean = word.any { it.isUpperCase() }


    /**
     * only letters, digits and underscore are allowed
     */
    private fun correctSymbolsAreUsed(word: String): Boolean {
        if (word.isASCIILettersAndDigits()) {
            return true
        } else {
            // underscores are allowed in some cases - see "exceptionForUnderscore"
            val wordFromPackage = word.replace("_", "")
            if (wordFromPackage.isASCIILettersAndDigits() && exceptionForUnderscore(wordFromPackage)) {
                return true
            }
        }
        return false
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
     * function simply checks that package name starts with a proper domain name, for example from "com.huawei"
     */
    private fun isDomainMatches(packageNameParts: List<ASTNode>): Boolean {
        val packageNamePrefix = DOMAIN_NAME.split(PACKAGE_SEPARATOR)
        if (packageNameParts.size < packageNamePrefix.size) return false

        for (i in packageNamePrefix.indices) {
            if (packageNameParts[i].text != packageNamePrefix[i]) {
                return false
            }
        }
        return true
    }

    // FixMe: check if proper line/char numbers are added
    /**
     * method for creating and inserting package name into the parentNode
     * Will create composite object = (Identifier + Dot) and insert it into the children list of parent node
     */
    private fun createAndInsertPackageName(parentNode: ASTNode, insertBeforeNode: ASTNode?, packageNameToInsert: List<String>) {
        var compositeElementWithNameAndDot: CompositeElement? = null
        var childDot: LeafPsiElement? = null
        var childPackageNamePart: LeafPsiElement?

        packageNameToInsert.forEach { name ->
            // creating Composite object = ((IDENTIFIER) + (DOT))
            compositeElementWithNameAndDot = CompositeElement(ElementType.REFERENCE_EXPRESSION)
            childDot = LeafPsiElement(ElementType.DOT, PACKAGE_SEPARATOR)
            childPackageNamePart = LeafPsiElement(ElementType.IDENTIFIER, name)

            // putting composite node first int the parent tree and adding IDENTIFIER and DOT as children to it after
            parentNode.addChild(compositeElementWithNameAndDot!!, insertBeforeNode)
            compositeElementWithNameAndDot!!.addChild(childPackageNamePart!!)
            compositeElementWithNameAndDot!!.addChild(childDot!!)
        }

        // removing extra DOT that is not needed if we were inserting the whole package name (not just a part) to the parent
        if (insertBeforeNode == null) {
            compositeElementWithNameAndDot!!.removeChild(childDot!!)
        }
    }

    private fun checkFilePathMatchesWithPackageName(packageNameParts: List<ASTNode>,
                                                    realName: List<String>,
                                                    autoCorrect: Boolean,
                                                    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (realName.isNotEmpty() && packageNameParts.map { node -> node.text } != realName) {
            if (confiRules.isRuleEnabled(PACKAGE_NAME_INCORRECT)) {
                emit(packageNameParts[0].startOffset,
                    "${PACKAGE_NAME_INCORRECT.warnText} ${realName.joinToString(PACKAGE_SEPARATOR)}", true)

                if (autoCorrect) {
                    val parentNode = packageNameParts[0].treeParent.treeParent
                    parentNode.getChildren(null).forEach { node -> parentNode.removeChild(node) }
                    createAndInsertPackageName(parentNode, null, realName)
                }
            }
        }
    }
}
