package rri.fixbot.ruleset.huawei.constants

enum class Warnings(val text: String, val id: Int) {
    PACKAGE_NAME_MISSING("no package name declared in a file", 1),
    PACKAGE_NAME_INCORRECT_CASE("package name should be completely in a lower case:", 2),
    PACKAGE_NAME_INCORRECT_PREFIX ("package name should start only from:", 3),
    PACKAGE_NAME_INCORRECT_SYMBOLS ("package name should contain only latin (ASCII) letters or numbers. For separation of words use dot: ", 4),

    CLASS_NAME_INCORRECT("class name should be in PascalCase and should contain only latin (ASCII) letters or numbers: ", 5),
    VARIABLE_NAME_INCORRECT_FORMAT("variable name should be in camel case (correct: checkIpConfig, incorrect: CheckIPConfig)" +
        " should contain only latin (ASCII) letters or numbers and should start from lower letter: ", 6),
    VARIABLE_NAME_INCORRECT("variable name should contain more than one letter: ", 7),
    CONSTANT_UPPERCASE("<val> properties from companion object or on file level mostly in all cases are constants - please use upper snake case for them: ", 8),
    VARIABLE_HAS_PREFIX("variable has prefix (like mVariable or M_VARIABLE), generally it is a bad code style (Android - is the only exception) : ", 9),
    IDENTIFIER_LENGTH("identifier's length is incorrect, it should be in range of [2, 64] symbols: ", 10),
    ENUM_VALUE("in the same way as constants, enum values should be in UPPER_CASE snake format: ", 11),
    GENERIC_NAME("generic name should contain only one single capital letter, it can be followed by a number:", 12),

    FUNCTION_NAME_INCORRECT_CASE("function/method name should be in lowerCamelCase: ", 13),
    FUNCTION_BOOLEAN_PREFIX("functions that return the value of Boolean type should have <is> or <has> prefix", 14)
}
