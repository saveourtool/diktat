package org.cqfn.diktat.ruleset.utils

import com.google.common.base.CaseFormat

/**
 * checking that string looks like: PascalCaseForClassName
 */
fun String.isPascalCase(): Boolean = this.matches("([A-Z][a-z0-9]+)+".toRegex())

/**
 * checking that string looks like: lowerCaseOfVariable
 */
fun String.isLowerCamelCase(): Boolean = this.matches("[a-z]([a-z0-9])*([A-Z][a-z0-9]+)*".toRegex())

/**
 * checking that string looks like: CORRECT_CASE_FOR_CONSTANTS
 */
fun String.isUpperSnakeCase(): Boolean = this.matches("(([A-Z]+)_*)+[A-Z0-9]*".toRegex())

/**
 * checking that string looks like: lower_case_for_script_names
 */
fun String.isLowerSnakeCase(): Boolean = this.matches("(([a-z]+)_*)+[a-z0-9]*".toRegex())

/**
 * detecting the case of _this_ String and converting it to the right UpperSnakeCase (UPPER_UNDERSCORE) case
 */
fun String.toUpperSnakeCase(): String {
    // PascalCase -> PASCAL_CASE
    if (this.isPascalCase()) return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    // lower -> LOWER
    if (this.all { it.isLowerCase() }) return this.toUpperCase()
    // lowerCamel -> LOWER_CAMEL
    if (this.isLowerCamelCase()) return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    // lower_snake -> LOWER_SNAKE
    if (this.isLowerSnakeCase()) return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, this)

    val idx = getFirstLetterOrDigit()
    if (idx != -1) {
        // any other format -> UPPER_SNAKE_CASE
        // [p]a[SC]a[_]l -> [P]A_[SC]_A_[L]
        return this[idx].toUpperCase().toString() + convertUnknownCaseToUpperSnake(this.substring(idx + 1))
    }

    log.error("Not able to fix case format for: $this")
    return this
}

/**
 * detecting the case of _this_ String and converting it to the right UpperSnakeCase (UPPER_UNDERSCORE) case
 */
@Suppress("ForbiddenComment")
fun String.toLowerCamelCase(): String {
    // PascalCase -> PASCAL_CASE
    if (this.isPascalCase()) return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, this)
    // lower -> LOWER
    if (this.all { it.isUpperCase() }) return this.toLowerCase()
    // lowerCamel -> LOWER_CAMEL
    if (this.isUpperSnakeCase()) return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)
    // lower_snake -> LOWER_SNAKE
    if (this.isLowerSnakeCase()) return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)

    val idx = getFirstLetterOrDigit()
    if (idx != -1) {
        // any other format -> camelCase
        // changing first letter to uppercase and replacing several uppercase letters in raw to lowercase:
        // example of change: [P]a[SC]a[_]l -> [p]a[Sc]a[L]
        // FixMe: there is some discussion on how lowerN_Case should be resolved: to lowerNcase or to lowernCase or lowerNCase (current version)
        return this[idx].toLowerCase().toString() + convertUnknownCaseToCamel(this.substring(idx + 1), this[idx].isUpperCase())
    }

    log.error("Not able to fix case format for: $this")
    return this
}

/**
 * detecting the case of _this_ String and converting it to the right PascalCase (UpperCamel) case
 */
@Suppress("ForbiddenComment")
fun String.toPascalCase(): String = when {
    all { it.isUpperCase() } -> {
        // all letters UPPER -> Upper
        this[0] + substring(1).toLowerCase()
    }
    all { it.isLowerCase() } -> {
        // all letters lower -> Lower
        this[0].toUpperCase() + substring(1)
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
            this[idx].toUpperCase().toString() + convertUnknownCaseToCamel(substring(idx + 1), true)
        } else {
            log.error("Not able to fix case format for: $this")
            this
        }
    }
}

private fun convertUnknownCaseToCamel(str: String, isFirstLetterCapital: Boolean): String {
    // [p]a[SC]a[_]l -> [P]a[Sc]a[L]
    var isPreviousLetterCapital = isFirstLetterCapital
    var isPreviousLetterUnderscore = false
    return str.map {
        if (it.isUpperCase()) {
            val result = if (isPreviousLetterCapital && !isPreviousLetterUnderscore) it.toLowerCase() else it
            isPreviousLetterCapital = true
            isPreviousLetterUnderscore = false
            result.toString()
        } else {
            val result = if (it == '_') {
                isPreviousLetterUnderscore = true
                ""
            } else if (isPreviousLetterUnderscore) {
                isPreviousLetterCapital = true
                isPreviousLetterUnderscore = false
                it.toUpperCase().toString()
            } else {
                isPreviousLetterCapital = false
                isPreviousLetterUnderscore = false
                it.toString()
            }
            result
        }
    }.joinToString("")
}

private fun convertUnknownCaseToUpperSnake(str: String): String {
    // [p]a[SC]a[_]l -> [P]A_[SC]_A_[L]
    var alreadyInsertedUnderscore = true
    return str.map {
        if (it.isUpperCase()) {
            if (!alreadyInsertedUnderscore) {
                alreadyInsertedUnderscore = true
                "_$it"
            } else {
                it.toString()
            }
        } else {
            alreadyInsertedUnderscore = (it == '_')
            it.toUpperCase().toString()
        }

    }.joinToString("")
}

private fun String.getFirstLetterOrDigit() =
        indexOfFirst { it.isLetterOrDigit() }
