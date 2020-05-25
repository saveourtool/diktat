package rri.fixbot.ruleset.huawei.utils

import com.google.common.base.CaseFormat


fun String.isJavaKeyWord() = Keywords.isJavaKeyWord(this)
fun String.isKotlinKeyWord() = Keywords.isKotlinKeyWord(this)

fun String.isASCIILettersAndDigits(): Boolean = this.all { it.isDigit() || it in 'A'..'Z' || it in 'a'..'z' }

fun String.isDigits(): Boolean = this.all { it.isDigit() }

fun String.isPascalCase(): Boolean = this.matches("([A-Z][a-z0-9]+)+".toRegex())

fun String.isLowerCamelCase(): Boolean = this.matches("[a-z]([a-z0-9])*([A-Z][a-z0-9]+)*".toRegex())

fun String.isUpperSnakeCase(): Boolean = this.matches("(([A-Z]+)_*)+[A-Z]*".toRegex())

fun String.isLowerSnakeCase(): Boolean = this.matches("(([a-z]+)_*)+[a-z]*".toRegex())

fun String.containsOneLetterOrZero(): Boolean {
    val count = this.count { it.isLetter() }
    return count == 1 || count == 0
}

fun String.splitPathToDirs(): List<String> =
    this.replace("\\", "/")
        .replace("//", "/")
        .split("/")

// method checks that string has prefix like:
// mFunction, kLength or M_VAR
fun String.hasPrefix(): Boolean {
    // checking cases like mFunction
    if (this.isLowerCamelCase() && this.length >= 2 && this.substring(0, 1).count { it in 'a'..'z' } == 1) return true
    // checking cases like M_VAL
    if (this.isUpperSnakeCase() && this.length >= 2 && this.substring(0, 1).count { it in 'A'..'Z' } == 1) return true
    return false
}

/**
 * removing the prefix in the word
 * M_VAR -> VAR
 * mVariable -> variable
 */
fun String.removePrefix(): String {
    // FixMe: there can be cases when after you will change variable name - it becomes a keyword
    if (this.isLowerCamelCase()) return this[1].toLowerCase() + this.substring(2)
    if (this.isUpperSnakeCase()) return this.substring(2)
    return this
}


/**
 * removing the prefix in the word
 * M_VAR -> VAR
 * mVariable -> variable
 */
fun String.toUpperCase(): String {
    // PascalCase -> PASCAL_CASE
    if (this.isPascalCase()) return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    // lower -> LOWER
    if (this.all{it.isLowerCase()}) return this.toUpperCase()
    // lowerCamel -> LOWER_CAMEL
    if (this.isLowerCamelCase()) return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    // lower_snake -> LOWER_SNAKE
    if (this.isLowerSnakeCase()) return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, this)

    return this
}

fun String.toLowerCamelCase(): String {
    // PascalCase -> PASCAL_CASE
    if (this.isPascalCase()) return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this)
    // lower -> LOWER
    if (this.all{it.isUpperCase()}) return this.toLowerCase()
    // lowerCamel -> LOWER_CAMEL
    if (this.isUpperSnakeCase()) return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, this)
    // lower_snake -> LOWER_SNAKE
    if (this.isLowerSnakeCase()) return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, this)

    return this
}
