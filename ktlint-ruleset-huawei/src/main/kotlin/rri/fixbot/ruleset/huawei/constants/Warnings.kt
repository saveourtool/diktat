package rri.fixbot.ruleset.huawei.constants

enum class Warnings(val text: String, val id: Int) {
    PACKAGE_NAME_MISSING("no package name declared in a file", 1),
    PACKAGE_NAME_INCORRECT_CASE("package name should be completely in a lower case:", 2),
    PACKAGE_NAME_INCORRECT_PREFIX ("package name should start from company's domain:", 3),
    PACKAGE_NAME_INCORRECT_SYMBOLS ("package name should contain only latin (ASCII) letters or numbers. For separation of words use dot: ", 4),
    PACKAGE_NAME_INCORRECT("package name does not match the directory hierarchy for this file, the path to the file is:", 5),

    CLASS_NAME_INCORRECT("class/enum/interface name should be in PascalCase and should contain only latin (ASCII) letters or numbers: ", 6),
    OBJECT_NAME_INCORRECT("object structure name should be in PascalCase and should contain only latin (ASCII) letters or numbers: ", 7),
    VARIABLE_NAME_INCORRECT_FORMAT("variable name should be in camel case (correct: checkIpConfig, incorrect: CheckIPConfig)" +
        " should contain only latin (ASCII) letters or numbers and should start from lower letter: ", 8),
    VARIABLE_NAME_INCORRECT("variable name should contain more than one letter: ", 9),
    CONSTANT_UPPERCASE("<val> properties from companion object or on file level mostly in all cases are constants - please use upper snake case for them: ", 10),
    VARIABLE_HAS_PREFIX("variable has prefix (like mVariable or M_VARIABLE), generally it is a bad code style (Android - is the only exception) : ", 11),
    IDENTIFIER_LENGTH("identifier's length is incorrect, it should be in range of [2, 64] symbols: ", 12),
    ENUM_VALUE("in the same way as constants, enum values should be in UPPER_CASE snake format: ", 13),
    GENERIC_NAME("generic name should contain only one single capital letter, it can be followed by a number:", 14),

    FUNCTION_NAME_INCORRECT_CASE("function/method name should be in lowerCamelCase: ", 15),
    FUNCTION_BOOLEAN_PREFIX("functions that return the value of Boolean type should have <is> or <has> prefix", 16),

    FILE_NAME_INCORRECT("file name is incorrect - it should end with .kt extension and be in PascalCase: ", 17),
    FILE_NAME_MATCH_CLASS("file name is incorrect - it should match with the class described in it if there is the only one class declared: ", 18),

    EXCEPTION_SUFFIX("all exception classes should have \"Exception\" suffix:", 19)
}
