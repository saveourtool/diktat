package com.huawei.rri.fixbot.ruleset.huawei.utils

import com.google.common.base.CaseFormat


fun String.isJavaKeyWord() = Keywords.isJavaKeyWord(this)
fun String.isKotlinKeyWord() = Keywords.isKotlinKeyWord(this)

fun String.isASCIILettersAndDigits(): Boolean = this.all { it.isDigit() || it in 'A'..'Z' || it in 'a'..'z' }

fun String.isDigits(): Boolean = this.all { it.isDigit() }

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




