package com.huawei.rri.fixbot.ruleset.huawei.constants

import config.rules.Rule
import config.rules.RulesConfig
import config.rules.isRuleEnabled

enum class Warnings(private val id: Int, private val canBeAutoCorrected: Boolean, private val warn: String) : Rule {
    // ======== chapter 1 ========
    PACKAGE_NAME_MISSING(1, true, "no package name declared in a file"),
    PACKAGE_NAME_INCORRECT_CASE(2, true, "package name should be completely in a lower case"),
    PACKAGE_NAME_INCORRECT_PREFIX(3, true, "package name should start from company's domain"),

    // FixMe: should add autofix
    PACKAGE_NAME_INCORRECT_SYMBOLS(4, false, "package name should contain only latin (ASCII) letters or numbers. For separation of words use dot"),
    PACKAGE_NAME_INCORRECT_PATH(5, true, "package name does not match the directory hierarchy for this file, the path to the file is"),

    CLASS_NAME_INCORRECT(6, true, "class/enum/interface name should be in PascalCase and should contain only latin (ASCII) letters or numbers"),
    OBJECT_NAME_INCORRECT(7, true, "object structure name should be in PascalCase and should contain only latin (ASCII) letters or numbers"),
    VARIABLE_NAME_INCORRECT_FORMAT(8, true, "variable name should be in camel case (correct: checkIpConfig, incorrect: CheckIPConfig)" +
        " should contain only latin (ASCII) letters or numbers and should start from lower letter"),
    VARIABLE_NAME_INCORRECT(9, false, "variable name should contain more than one letter"),
    CONSTANT_UPPERCASE(10, true, "<val> properties from companion object or on file level mostly in all cases are constants - please use upper snake case for them"),
    VARIABLE_HAS_PREFIX(11, true, "variable has prefix (like mVariable or M_VARIABLE), generally it is a bad code style (Android - is the only exception)"),
    IDENTIFIER_LENGTH(12, false, "identifier's length is incorrect, it should be in range of [2, 64] symbols"),
    ENUM_VALUE(13, true, "in the same way as constants, enum values should be in UPPER_CASE snake format"),
    GENERIC_NAME(14, true, "generic name should contain only one single capital letter, it can be followed by a number"),

    FUNCTION_NAME_INCORRECT_CASE(15, true, "function/method name should be in lowerCamelCase"),
    FUNCTION_BOOLEAN_PREFIX(16, true, "functions that return the value of Boolean type should have <is> or <has> prefix"),

    FILE_NAME_INCORRECT(17, true, "file name is incorrect - it should end with .kt extension and be in PascalCase"),
    FILE_NAME_MATCH_CLASS(18, true, "file name is incorrect - it should match with the class described in it if there is the only one class declared"),

    EXCEPTION_SUFFIX(19, true, "all exception classes should have \"Exception\" suffix"),

    // ======== chapter 2 ========
    MISSING_KDOC_TOP_LEVEL(20, false, "all public and internal top-level classes and functions should have Kdoc"),
    MISSING_KDOC_CLASS_ELEMENTS(21, false, "all public, internal and protected classes, functions and variables inside the class should have Kdoc"),
    KDOC_WITHOUT_PARAM_TAG(22, true, "all methods which take arguments should have @param tags in KDoc, the following parameters are missing"),
    KDOC_WITHOUT_RETURN_TAG(23, true, "all methods which return values should have @return tag in KDoc"),
    KDOC_WITHOUT_THROWS_TAG(24, true, "all methods which throw exceptions should have @throws tag in KDoc"),
    BLANK_LINE_AFTER_KDOC(25, true, "there should be no empty line between Kdoc and code it is describing")
    ;

    override fun ruleName(): String = this.name

    fun warnText(): String = "[${this.id}] ${this.warn}:"

    fun warnAndFix(configRules: List<RulesConfig>,
                   emit: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit),
                   isFixMode: Boolean,
                   freeText: String,
                   offset: Int,
                   autoFix: () -> Unit) {
        warn(configRules, emit, this.canBeAutoCorrected, freeText, offset)
        fix(configRules, autoFix, isFixMode)
    }

    fun warn(configs: List<RulesConfig>,
                     emit: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit),
                     autoCorrected: Boolean,
                     freeText: String,
                     offset: Int) {
        if (configs.isRuleEnabled(this)) {
            emit(offset,
                "${this.warnText()} $freeText",
                autoCorrected
            )
        }
    }

    private inline fun fix(configs: List<RulesConfig>, autoFix: () -> Unit, isFix: Boolean) {
        if (configs.isRuleEnabled(this) && isFix) {
            autoFix()
        }
    }
}
