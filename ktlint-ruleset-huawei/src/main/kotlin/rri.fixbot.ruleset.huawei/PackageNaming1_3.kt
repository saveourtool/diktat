package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import rri.fixbot.ruleset.huawei.utils.isJavaKeyWord
import rri.fixbot.ruleset.huawei.utils.isKotlinKeyWord

/**
 * Rule 1.3: package name is in lower case and separated by dots, code developed internally in Huawei should start
 * with com.huawei, and the package name is allowed to have numbers
 *
 * Current limitations and FixMe:
 * need to support autofixing of directories in the same way as package is named. For example if we have package name:
 * package a.b.c.D -> then class D should be placed in a/b/c/ directories
 */

class PackageNaming1_3 : Rule("huawei-package-naming") {
    companion object {
        const val PACKAGE_SEPARATOR = "."
        const val PACKAGE_STARTS_WITH = "com.huawei"
    }

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == ElementType.PACKAGE_DIRECTIVE) {
            val wordsInPackageName = node.getChildren(null)
                // get the package name after removing whitespace nodes and package keyword
                .filter { it.elementType != ElementType.PACKAGE_KEYWORD && !it.chars.toString().isBlank() }[0]
                // get the value from node and split by a package separator (dot)
                .chars.toString()
                .split(PACKAGE_SEPARATOR)

            checkPackageName(node, autoCorrect, wordsInPackageName, emit)
        }

        // same logic should be checked for all imports that start with $PACKAGE_STARTS_WITH
        if (node.elementType == ElementType.IMPORT_DIRECTIVE) {
            val wordsInPackageName = node.getChildren(null)
                .filter { it.elementType != ElementType.IMPL_KEYWORD && !it.chars.toString().isBlank() }
                .map { it.chars.toString() }
                // should take the only [0] node - it will be the package name
                .filter { it.startsWith(PACKAGE_STARTS_WITH) }[0]
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

        // all words should be in a lower case (checkMe!)
        if (!wordsInPackageName.all { word -> isWordInLowerCase(word) }) {
            emit(
                node.startOffset,
                "package name should be completely in a lower case",
                true
            )
        }

        // package name should start from a company's domain name
        if ("${wordsInPackageName[0]}.${wordsInPackageName[1]}" != PACKAGE_STARTS_WITH) {
            emit(
                node.startOffset,
                "package name should start only from $PACKAGE_STARTS_WITH ",
                true
            )
        }

        // all words should contain only letters or digits
        if (!wordsInPackageName.all { word -> correctCharsAreUsed(word) }) {
            emit(
                node.startOffset,
                "package name should contain only english letters or numbers. For separation of words use dot.",
                true
            )
        }
    }

    private fun isWordInLowerCase(word: String): Boolean {
        return word.find { !(it.isLowerCase() || it.isDigit()) } == null
    }

    private fun isOnlyLettersAndDigits(word: String): Boolean {
        return word.find { !(it in 'a'..'z' || it in '0'..'9') } == null
    }

    private fun correctCharsAreUsed(word: String): Boolean {

        if (isOnlyLettersAndDigits(word)) {
            return true
        } else {
            val wordFromPackage = word.replace("_", "")
            if (isOnlyLettersAndDigits(wordFromPackage) && exceptionForUnderscore(wordFromPackage)) {
                return true
            }
        }
        return false
    }

    // In some cases, if the package name starts with a number or other characters,
    // but these characters cannot be used at the beginning of the Java/Kotlin package name,
    // or the package name contains reserved Java keywords, underscores are allowed.
    // For example: org.example.hyphenated_name,int_.example, com.example._123name
    private fun exceptionForUnderscore(word: String): Boolean {
        val wordFromPackage = word.replace("_", "")

        return wordFromPackage[0].isDigit() ||
            wordFromPackage.isKotlinKeyWord() ||
            wordFromPackage.isJavaKeyWord()
    }
}
