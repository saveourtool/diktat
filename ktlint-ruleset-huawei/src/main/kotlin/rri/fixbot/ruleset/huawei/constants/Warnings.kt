package rri.fixbot.ruleset.huawei.constants

enum class Warnings(val text: String, val id: Int) {
    PACKAGE_NAME_MISSING("no package name declared in a file", 1),
    PACKAGE_NAME_INCORRECT_CASE("package name should be completely in a lower case", 2),
    PACKAGE_NAME_INCORRECT_PREFIX ("package name should start only from", 3),
    PACKAGE_NAME_INCORRECT_SYMBOLS ("package name should contain only latin letters or numbers. For separation of words use dot.", 4)
}
