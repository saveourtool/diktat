@file:Suppress("FILE_NAME_MATCH_CLASS", "MatchingDeclarationName")

package com.saveourtool.diktat.ruleset.utils

import com.google.common.base.CaseFormat
import io.github.oshai.kotlinlogging.KotlinLogging

import java.util.Locale

private val log = KotlinLogging.logger {}

/**
 * Available cases to name enum members
 * @property str
 */
enum class Style(val str: String) {
    PASCAL_CASE("PascalCase"),
    SNAKE_CASE("UPPER_SNAKE_CASE"),
    ;
}

/**
 * checking that string looks like: PascalCaseForClassName
 *
 * @return boolean result
 */
fun String.isPascalCase(): Boolean = this.matches("([A-Z][a-z0-9]+)+".toRegex())

/**
 * checking that string looks like: lowerCaseOfVariable
 *
 * @return boolean result
 */
fun String.isLowerCamelCase(): Boolean = this.matches("[a-z]([a-z0-9])*([A-Z][a-z0-9]+)*".toRegex())

/**
 * checking that string looks like: CORRECT_CASE_FOR_CONSTANTS
 *
 * @return boolean result
 */
fun String.isUpperSnakeCase(): Boolean = this.matches("(([A-Z0-9]+)_*)+[A-Z0-9]*".toRegex())

/**
 * checking that string looks like: lower_case_for_script_names
 *
 * @return boolean result
 */
fun String.isLowerSnakeCase(): Boolean = this.matches("(([a-z]+)_*)+[a-z0-9]*".toRegex())

/**
 * detecting the case of _this_ String and converting it to the right UpperSnakeCase (UPPER_UNDERSCORE) case
 *
 * @return converted string
 */
fun String.toUpperSnakeCase(): String {
    // PascalCase -> PASCAL_CASE
    if (this.isPascalCase()) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    }
    // lower -> LOWER
    if (this.all { it.isLowerCase() }) {
        return this.uppercase(Locale.getDefault())
    }
    // lowerCamel -> LOWER_CAMEL
    if (this.isLowerCamelCase()) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    }
    // lower_snake -> LOWER_SNAKE
    if (this.isLowerSnakeCase()) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, this)
    }

    val idx = getFirstLetterOrDigit()
    if (idx != -1) {
        // any other format -> UPPER_SNAKE_CASE
        // [p]a[SC]a[_]l -> [P]A_[SC]_A_[L]
        return this[idx].uppercaseChar().toString() + convertUnknownCaseToUpperSnake(this.substring(idx + 1))
    }

    log.error { "Not able to fix case format for: $this" }
    return this
}

/**
 * detecting the case of _this_ String and converting it to the right UpperSnakeCase (UPPER_UNDERSCORE) case
 *
 * @return converted string
 */
@Suppress("ForbiddenComment")
fun String.toLowerCamelCase(): String {
    // PascalCase -> PASCAL_CASE
    if (this.isPascalCase()) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, this)
    }
    // lower -> LOWER
    if (this.all { it.isUpperCase() }) {
        return this.lowercase(Locale.getDefault())
    }
    // lowerCamel -> LOWER_CAMEL
    if (this.isUpperSnakeCase()) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)
    }
    // lower_snake -> LOWER_SNAKE
    if (this.isLowerSnakeCase()) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)
    }

    val idx = getFirstLetterOrDigit()
    if (idx != -1) {
        // any other format -> camelCase
        // changing first letter to uppercase and replacing several uppercase letters in raw to lowercase:
        // example of change: [P]a[SC]a[_]l -> [p]a[Sc]a[L]
        // FixMe: there is some discussion on how lowerN_Case should be resolved: to lowerNcase or to lowernCase or lowerNCase (current version)
        return this[idx].lowercaseChar().toString() + convertUnknownCaseToCamel(this.substring(idx + 1), this[idx].isUpperCase())
    }

    log.error { "Not able to fix case format for: $this" }
    return this
}

/**
 * detecting the case of _this_ String and converting it to the right PascalCase (UpperCamel) case
 *
 * @return converted string
 */
@Suppress("ForbiddenComment")
fun String.toPascalCase(): String = when {
    all { it.isUpperCase() } -> {
        // all letters UPPER -> Upper
        this[0] + substring(1).lowercase(Locale.getDefault())
    }
    all { it.isLowerCase() } -> {
        // all letters lower -> Lower
        this[0].uppercaseChar() + substring(1)
    }
    isUpperSnakeCase() -> {
        // lowerCamel -> LowerCamel
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this)
    }
    isLowerSnakeCase() -> {
        // lower_snake -> LowerSnake
        CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this)
    }
    else -> {
        val idx = getFirstLetterOrDigit()
        if (idx != -1) {
            // any other format -> PascalCase
            // changing first letter to uppercase and replacing several uppercase letters in raw to lowercase:
            // example of change: [p]a[SC]a[_]l -> [P]a[Sc]a[L]
            // FixMe: there is some discussion on how PascalN_Case should be resolved: to PascalNcase or to PascalnCase or PascalNCase (current version)
            this[idx].uppercaseChar().toString() + convertUnknownCaseToCamel(substring(idx + 1), true)
        } else {
            log.error { "Not able to fix case format for: $this" }
            this
        }
    }
}

/**
 * @return index of first character which is a letter or a digit
 */
private fun String.getFirstLetterOrDigit() =
    indexOfFirst { it.isLetterOrDigit() }

private fun convertUnknownCaseToCamel(str: String, isFirstLetterCapital: Boolean): String {
    // [p]a[SC]a[_]l -> [P]a[Sc]a[L]
    var isPreviousLetterCapital = isFirstLetterCapital
    var isPreviousLetterUnderscore = false
    return str.map { char ->
        if (char.isUpperCase()) {
            val result = if (isPreviousLetterCapital && !isPreviousLetterUnderscore) char.lowercaseChar() else char
            isPreviousLetterCapital = true
            isPreviousLetterUnderscore = false
            result.toString()
        } else {
            val result = when {
                char == '_' -> {
                    isPreviousLetterUnderscore = true
                    ""
                }
                isPreviousLetterUnderscore -> {
                    isPreviousLetterCapital = true
                    isPreviousLetterUnderscore = false
                    char.uppercaseChar().toString()
                }
                else -> {
                    isPreviousLetterCapital = false
                    isPreviousLetterUnderscore = false
                    char.toString()
                }
            }
            result
        }
    }.joinToString("")
}

private fun convertUnknownCaseToUpperSnake(str: String): String {
    // [p]a[SC]a[_]l -> [P]A_[SC]_A_[L]
    var alreadyInsertedUnderscore = true
    return str.map { char ->
        if (char.isUpperCase()) {
            if (!alreadyInsertedUnderscore) {
                alreadyInsertedUnderscore = true
                "_$char"
            } else {
                char.toString()
            }
        } else {
            alreadyInsertedUnderscore = (char == '_')
            char.uppercaseChar().toString()
        }
    }.joinToString("")
}
