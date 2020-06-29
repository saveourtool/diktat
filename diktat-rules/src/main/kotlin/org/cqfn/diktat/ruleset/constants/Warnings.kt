package org.cqfn.diktat.ruleset.constants

import org.cqfn.diktat.common.config.rules.Rule
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.isRuleEnabled

enum class Warnings(private val id: Int, private val canBeAutoCorrected: Boolean, private val warn: String) : Rule {
    // ======== chapter 1 ========
    PACKAGE_NAME_MISSING(1, true, "no package name declared in a file"),
    PACKAGE_NAME_INCORRECT_CASE(2, true, "package name should be completely in a lower case"),
    PACKAGE_NAME_INCORRECT_PREFIX(3, true, "package name should start from company's domain"),

    // FixMe: should add autofix
    PACKAGE_NAME_INCORRECT_SYMBOLS(4, false, "package name should contain only latin (ASCII) letters or numbers. For separation of words use dot"),
    PACKAGE_NAME_INCORRECT_PATH(5, true, "package name does not match the directory hierarchy for this file, the path to the file is"),
    INCORRECT_PACKAGE_SEPARATOR(6, true, "package name parts should be separated only by dots - there should be no other symbols like underscores (_)"),

    CLASS_NAME_INCORRECT(7, true, "class/enum/interface name should be in PascalCase and should contain only latin (ASCII) letters or numbers"),
    OBJECT_NAME_INCORRECT(8, true, "object structure name should be in PascalCase and should contain only latin (ASCII) letters or numbers"),
    VARIABLE_NAME_INCORRECT_FORMAT(9, true, "variable name should be in camel case (correct: checkIpConfig, incorrect: CheckIPConfig)" +
        " should contain only latin (ASCII) letters or numbers and should start from lower letter"),
    VARIABLE_NAME_INCORRECT(10, false, "variable name should contain more than one letter"),
    CONSTANT_UPPERCASE(11, true, "<val> properties from companion object or on file level mostly in all cases are constants - please use upper snake case for them"),
    VARIABLE_HAS_PREFIX(12, true, "variable has prefix (like mVariable or M_VARIABLE), generally it is a bad code style (Android - is the only exception)"),
    IDENTIFIER_LENGTH(13, false, "identifier's length is incorrect, it should be in range of [2, 64] symbols"),
    ENUM_VALUE(14, true, "in the same way as constants, enum values should be in UPPER_CASE snake format"),
    GENERIC_NAME(15, true, "generic name should contain only one single capital letter, it can be followed by a number"),

    FUNCTION_NAME_INCORRECT_CASE(16, true, "function/method name should be in lowerCamelCase"),
    FUNCTION_BOOLEAN_PREFIX(17, true, "functions that return the value of Boolean type should have <is> or <has> prefix"),

    FILE_NAME_INCORRECT(18, true, "file name is incorrect - it should end with .kt extension and be in PascalCase"),
    FILE_NAME_MATCH_CLASS(19, true, "file name is incorrect - it should match with the class described in it if there is the only one class declared"),

    EXCEPTION_SUFFIX(20, true, "all exception classes should have \"Exception\" suffix"),

    // ======== chapter 2 ========
    MISSING_KDOC_TOP_LEVEL(21, false, "all public and internal top-level classes and functions should have Kdoc"),
    MISSING_KDOC_CLASS_ELEMENTS(22, false, "all public, internal and protected classes, functions and variables inside the class should have Kdoc"),
    MISSING_KDOC_ON_FUNCTION(23, true, "all public, internal and protected functions should have Kdoc with proper tags"),
    KDOC_WITHOUT_PARAM_TAG(24, true, "all methods which take arguments should have @param tags in KDoc"),
    KDOC_WITHOUT_RETURN_TAG(25, true, "all methods which return values should have @return tag in KDoc"),
    KDOC_WITHOUT_THROWS_TAG(26, true, "all methods which throw exceptions should have @throws tag in KDoc"),
    BLANK_LINE_AFTER_KDOC(27, true, "there should be no empty line between Kdoc and code it is describing"),
    KDOC_EMPTY_KDOC(28, false, "KDoc should never be empty"),
    KDOC_WRONG_SPACES_AFTER_TAG(29, true, "there should be exactly one white space after tag name in KDoc"),
    KDOC_WRONG_TAGS_ORDER(30, true, "in KDoc standard tags are arranged in order @param, @return, @throws, but are"),
    KDOC_NEWLINES_BEFORE_BASIC_TAGS(31, true, "in KDoc block of standard tags @param, @return, @throws should contain newline before only if there is other content before it"),
    KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS(32, true, "in KDoc standard tags @param, @return, @throws should not containt newline between them, but these tags do"),
    KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS(33, true, "in KDoc there should be exactly one empty line after special tags"),
    KDOC_NO_EMPTY_TAGS(34, false, "no empty descriptions in tag blocks are allowed"),
    KDOC_NO_DEPRECATED_TAG(35, true, "KDoc doesn't support @deprecated tag, use @Deprecated annotation instead"),
    HEADER_WRONG_FORMAT(36, true, "file header comments should be properly formatted"),
    HEADER_MISSING_OR_WRONG_COPYRIGHT(37, true, "file header comment must include copyright information inside a block comment"),
    HEADER_CONTAINS_DATE_OR_AUTHOR(38, false, "file header comment should not contain creation date and author name"),
    HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE(39, false, "files that contain multiple or no classes should contain description of what is inside of this file"),
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
