package rri.fixbot.ruleset.huawei.huawei.utils


fun String.isJavaKeyWord() = Keywords.isJavaKeyWord(this)
fun String.isKotlinKeyWord() = Keywords.isKotlinKeyWord(this)

fun String.isASCIILettersAndDigits(): Boolean = this.all { it.isDigit() || it in 'A'..'Z' || it in 'a'..'z' }

fun String.isDigits(): Boolean = this.all { it.isDigit() }

fun String.isPascalCase(): Boolean = this.matches("([A-Z][a-z0-9]+)+".toRegex())

fun String.isLowerCamelCase(): Boolean = this.matches("[a-z]([a-z0-9])*([A-Z][a-z0-9]+)*".toRegex())

fun String.isUpperSnakeCase(): Boolean = this.matches("(([A-Z]+)_*)+[A-Z]*".toRegex())

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
    if (this.isLowerCamelCase() && this.length >= 2 && this.substring(0, 1).count { it in 'a'..'z' } == 2) return true
    if (this.isUpperSnakeCase() && this.length >= 2 && this.substring(0, 1).count { it in 'A'..'Z' } == 2) return true
    return false
}
