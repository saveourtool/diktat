package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.org.jdom.Element
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.huawei.utils.getAllLLeafsWithSpecificType
import rri.fixbot.ruleset.huawei.huawei.utils.isASCIILettersAndDigits
import rri.fixbot.ruleset.huawei.huawei.utils.isJavaKeyWord
import rri.fixbot.ruleset.huawei.huawei.utils.isKotlinKeyWord

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
    }

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (node.elementType == ElementType.PACKAGE_DIRECTIVE) {
            // if node isLeaf - this means that there is no package name declared
            if (node.isLeaf()) {
                emit(node.startOffset, PACKAGE_NAME_MISSING.text, true)
                // FixMe: can be fixed automatically by checking current directory
                return
            }


            // getting all identifiers from package name into the list like [com, huawei, project]
            val wordsInPackageName = mutableListOf<ASTNode>()
            node.getAllLLeafsWithSpecificType(ElementType.IDENTIFIER, wordsInPackageName)

            // no need to check that packageIdentifiers is empty, because in this case parsing will fail
            checkPackageName(autoCorrect, wordsInPackageName, emit)
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
                // also need to move this autofix to a new function
                val parentNodeToInsert = wordsInPackageName[0].treeParent.treeParent
                createNodesForDomainName().forEach { packageName ->
                    val compositeElementWithNameAndDot = CompositeElement(ElementType.REFERENCE_EXPRESSION)
                    parentNodeToInsert.addChild(compositeElementWithNameAndDot, wordsInPackageName[0].treeParent)
                    compositeElementWithNameAndDot.addChild(packageName)
                    compositeElementWithNameAndDot.addChild(LeafPsiElement(ElementType.DOT, "."))
                }
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
    private fun createNodesForDomainName(): List<ASTNode> {
        return DOMAIN_NAME
            .split(PACKAGE_SEPARATOR)
            .map { name -> LeafPsiElement(ElementType.IDENTIFIER, name) }
    }
}
