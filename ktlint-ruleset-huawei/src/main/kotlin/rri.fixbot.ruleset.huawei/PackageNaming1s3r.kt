package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.isLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.huawei.utils.*
import org.slf4j.LoggerFactory

/**
 * Rule 1.3: package name is in lower case and separated by dots, code developed internally in your company (in example Huawei) should start
 * with it's domain (like com.huawei), and the package name is allowed to have numbers
 *
 * Current limitations and FixMe:
 * need to support autofixing of directories in the same way as package is named. For example if we have package name:
 * package a.b.c.D -> then class D should be placed in a/b/c/ directories
 */

class PackageNaming1s3r : Rule("package-naming") {
    companion object {
        // FixMe: these constants should be moved to configuration file
        const val PACKAGE_SEPARATOR = "."
        const val DOMAIN_NAME = "com.huawei"
        val TECHNICAL_DIR_NAMES = listOf("src", "main", "java", "kotlin")
        private val log = LoggerFactory.getLogger(PackageNaming1s3r::class.java)
    }

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        if (node.elementType == ElementType.PACKAGE_DIRECTIVE) {
            val filePathParts = params.fileName?.splitPathToDirs()
            val basePath = params.projectBaseDir

            val realPackageName = if (filePathParts == null || basePath == null) {
                log.error("Not able to determine a path of a scanned file or basic path ")
                null
            } else {
                // creating a real package name:
                // 1) getting a path after the base project directory
                // 2) removing src/main/kotlin/java/e.t.c dirs
                filePathParts.subList(filePathParts.indexOf(basePath), filePathParts.size)
                    .filter { !TECHNICAL_DIR_NAMES.contains(it) }
            }

            // if node isLeaf - this means that there is no package name declared
            if (node.isLeaf()) {
                emit(node.startOffset, PACKAGE_NAME_MISSING.text, true)
                if (autoCorrect) {
                    formAndInsertPackageName(node, null, realPackageName)
                }
                return
            }

            // getting all identifiers from package name into the list like [com, huawei, project]
            val wordsInPackageName = mutableListOf<ASTNode>()
            node.getAllLLeafsWithSpecificType(ElementType.IDENTIFIER, wordsInPackageName)

            // no need to check that packageIdentifiers is empty, because in this case parsing will fail
            checkPackageName(autoCorrect, wordsInPackageName, emit)
            checkFilePathMatchesWithPackage(wordsInPackageName, params)
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
                emit(it.startOffset, "${PACKAGE_NAME_INCORRECT_CASE.text} ${it.text}", true)
                if (autoCorrect) {
                    (it as LeafPsiElement).replaceWithText(it.text.toLowerCase())
                }
            }

        // package name should start from a company's domain name
        if (!isDomainMatches(wordsInPackageName)) {
            emit(wordsInPackageName[0].startOffset, "${PACKAGE_NAME_INCORRECT_PREFIX.text} $DOMAIN_NAME", true)
            if (autoCorrect) {
                // FixMe: .treeParent.treeParent is called to get DOT_QUALIFIED_EXPRESSION - it can be done in more elegant way
                val parentNodeToInsert = wordsInPackageName[0].treeParent.treeParent
                formAndInsertPackageName(parentNodeToInsert, wordsInPackageName[0].treeParent, DOMAIN_NAME.split(PACKAGE_SEPARATOR))
            }
        }

        // all words should contain only letters or digits
        wordsInPackageName.filter { word -> !correctSymbolsAreUsed(word.text) }.forEach {
            emit(it.startOffset, "${PACKAGE_NAME_INCORRECT_SYMBOLS.text} ${it.text}", true)
            /*   if (autoCorrect) {
                // FixMe: cover with tests
                wordsInPackageNameConverted = wordsInPackageNameConverted.map { it.replace("_", ".").replace("-", ".") }
                (node as LeafPsiElement).rawReplaceWithText(wordsInPackageNameConverted.joinToString(PACKAGE_SEPARATOR))
            }*/
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
    private fun formAndInsertPackageName(parentNode: ASTNode, insertBeforeNode: ASTNode?, packageParts: List<String>?) {
        packageParts
            ?.map { name -> LeafPsiElement(ElementType.IDENTIFIER, name) }
            ?.forEach { packagePart ->
                // creating Composite object = ((IDENTIFIER) + (DOT))
                val compositeElementWithNameAndDot = CompositeElement(ElementType.REFERENCE_EXPRESSION)
                // putting it parent tree and adding IDENTIFIER and DOT as children to it
                parentNode.addChild(compositeElementWithNameAndDot, insertBeforeNode)
                compositeElementWithNameAndDot.addChild(packagePart)
                compositeElementWithNameAndDot.addChild(LeafPsiElement(ElementType.DOT, PACKAGE_SEPARATOR))
            }
    }

    //FixMe: check to compare real package name with generated
    private fun checkFilePathMatchesWithPackage(packageNameParts: List<ASTNode>, params: KtLint.Params) {
    }
}
