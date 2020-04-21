package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.isLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import rri.fixbot.ruleset.huawei.constants.Warnings.*
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
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {

        if (node.elementType == ElementType.PACKAGE_DIRECTIVE) {
            if (node.isLeaf()) {
                emit(node.startOffset, PACKAGE_NAME_MISSING.text, true)

                return
            }

            val wordsInPackageName = node.getChildren(null)
                // get the package name after removing whitespaces and package keyword
                .filter { it.elementType != ElementType.PACKAGE_KEYWORD && !it.chars.toString().isBlank() }[0]
                // get the value from node and split by a package separator (dot)
                .chars.toString()
                .split(PACKAGE_SEPARATOR)

            checkPackageName(node, autoCorrect, wordsInPackageName, emit)
        }
    }

    private fun checkPackageName(
        node: ASTNode,
        autoCorrect: Boolean,
        wordsInPackageName: List<String>,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {

        // all words should be in a lower case (lower case letters/digits/underscore)
        if (wordsInPackageName.any { word -> !isLowerCaseOrDigit(word.replace("_", "")) }) {
            emit(node.startOffset, PACKAGE_NAME_INCORRECT_CASE.text, true)
        }

        // package name should start from a company's domain name
        if (!isDomainMatches(wordsInPackageName)) {
            emit(node.startOffset, "${PACKAGE_NAME_INCORRECT_PREFIX.text} $DOMAIN_NAME", true)
        }

        // all words should contain only letters or digits
        if (wordsInPackageName.any { word -> !correctSymbolsAreUsed(word) }) {
            emit(node.startOffset, PACKAGE_NAME_INCORRECT_SYMBOLS.text, true)
        }
    }

    private fun isLowerCaseOrDigit(word: String): Boolean = !word.any { !(it.isLowerCase() || it.isDigit()) }

    private fun isLatinLettersAndDigits(word: String): Boolean = !word.any { !(it.isDigit() || it in 'A'..'Z' || it in 'a'..'z') }

    /**
     * only letters, digits and underscore are allowed
     */
    private fun correctSymbolsAreUsed(word: String): Boolean {
        if (isLatinLettersAndDigits(word)) {
            return true
        } else {
            // underscores are allowed in some cases - see "exceptionForUnderscore"
            val wordFromPackage = word.replace("_", "")
            if (isLatinLettersAndDigits(wordFromPackage) && exceptionForUnderscore(wordFromPackage)) {
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
    private fun isDomainMatches(packageNameParts: List<String>): Boolean {
        val packageNamePrefix = DOMAIN_NAME.split(PACKAGE_SEPARATOR)

        if (packageNameParts.size < packageNamePrefix.size) return false

        for (i in packageNamePrefix.indices) {
            if (packageNameParts[i] != packageNamePrefix[i]) {
                return false
            }
        }
        return true
    }
}
