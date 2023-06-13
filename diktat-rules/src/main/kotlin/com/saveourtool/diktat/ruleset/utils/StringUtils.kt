/**
 * Utility methods and constants to work with strings
 */

package com.saveourtool.diktat.ruleset.utils

import org.jetbrains.kotlin.lexer.KtTokens

internal const val NEWLINE = '\n'

internal const val SPACE = ' '

internal const val TAB = '\t'

@Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
val JAVA = arrayOf("abstract", "assert", "boolean",
    "break", "byte", "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else", "extends", "false",
    "final", "finally", "float", "for", "goto", "if", "implements",
    "import", "instanceof", "int", "interface", "long", "native",
    "new", "null", "package", "private", "protected", "public",
    "return", "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "true",
    "try", "void", "volatile", "while")

@Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
val KOTLIN = KtTokens.KEYWORDS
    .types
    .map { line -> line.toString() }
    .plus(KtTokens.SOFT_KEYWORDS.types.map { line -> line.toString() })

/**
 * Either `log` or `logger`, case-insensitive.
 *
 * A name like `psychologist` or `LOGIN` won't be matched by this regular
 * expression.
 */
val loggerPropertyRegex = "(?iu)^log(?:ger)?$".toRegex()

/**
 * @return whether [this] string represents a Java keyword
 */
fun String.isJavaKeyWord() = JAVA.contains(this)

/**
 * @return whether [this] string represents a Kotlin keyword
 */
fun String.isKotlinKeyWord() = KOTLIN.contains(this)

/**
 * @return whether [this] string contains only ASCII letters and/or digits
 */
@Suppress("FUNCTION_NAME_INCORRECT_CASE")
fun String.isASCIILettersAndDigits(): Boolean = this.all { it.isDigit() || it in 'A'..'Z' || it in 'a'..'z' }

/**
 * @return whether [this] string contains only digits
 */
fun String.isDigits(): Boolean = this.all { it.isDigit() }

/**
 * @return whether [this] string contains any uppercase letters
 */
fun String.hasUppercaseLetter(): Boolean = this.any { it.isUpperCase() }

/**
 * @return whether [this] string contains exactly one or zero letters
 */
@Suppress("FUNCTION_BOOLEAN_PREFIX")
fun String.containsOneLetterOrZero(): Boolean {
    val count = this.count { it.isLetter() }
    return count == 1 || count == 0
}

/**
 * method checks that string has prefix like:
 * mFunction, kLength or M_VAR
 *
 * @return true if string has prefix
 */
@Suppress("ForbiddenComment")
fun String.hasPrefix(): Boolean {
    // checking cases like mFunction
    if (this.isLowerCamelCase() && this.length >= 2 && this.substring(0, 2).count { it in 'A'..'Z' } == 1) {
        return true
    }
    // checking cases like M_VAL
    if (this.isUpperSnakeCase() && this.length >= 2 && this.substring(0, 2).contains('_')) {
        return true
    }
    return false
}

/**
 * removing the prefix in the word
 * M_VAR -> VAR
 * mVariable -> variable
 *
 * @return a string without prefix
 */
@Suppress("ForbiddenComment")
fun String.removePrefix(): String {
    // FixMe: there can be cases when after you will change variable name - it becomes a keyword
    if (this.isLowerCamelCase()) {
        return this[1].lowercaseChar() + this.substring(2)
    }
    if (this.isUpperSnakeCase()) {
        return this.substring(2)
    }
    return this
}

/**
 * @return the indentation of the last line of this string.
 */
internal fun String.lastIndent() = substringAfterLast(NEWLINE).count(::isSpaceCharacter)

/**
 * @return the number of leading space characters in this string.
 */
internal fun String.leadingSpaceCount(): Int =
    asSequence()
        .takeWhile(::isSpaceCharacter)
        .count()

/**
 * @param ch the character to examine.
 * @return `true` if [ch] is a [SPACE], `false` otherwise.
 */
private fun isSpaceCharacter(ch: Char): Boolean =
    ch == SPACE
