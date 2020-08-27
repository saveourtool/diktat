package org.cqfn.diktat.ruleset.constants

import org.cqfn.diktat.common.config.rules.Rule
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.isRuleEnabled

@Suppress("ForbiddenComment", "MagicNumber")
enum class Warnings(private val canBeAutoCorrected: Boolean, private val warn: String) : Rule {
    // ======== chapter 1 ========
    PACKAGE_NAME_MISSING(true, "no package name declared in a file"),
    PACKAGE_NAME_INCORRECT_CASE(true, "package name should be completely in a lower case"),
    PACKAGE_NAME_INCORRECT_PREFIX(true, "package name should start from company's domain"),
    // FixMe: should add autofix
    PACKAGE_NAME_INCORRECT_SYMBOLS(false, "package name should contain only latin (ASCII) letters or numbers. For separation of words use dot"),
    PACKAGE_NAME_INCORRECT_PATH(true, "package name does not match the directory hierarchy for this file, the real package name should be:"),
    INCORRECT_PACKAGE_SEPARATOR(true, "package name parts should be separated only by dots - there should be no other symbols like underscores (_)"),
    CLASS_NAME_INCORRECT(true, "class/enum/interface name should be in PascalCase and should contain only latin (ASCII) letters or numbers"),
    OBJECT_NAME_INCORRECT(true, "object structure name should be in PascalCase and should contain only latin (ASCII) letters or numbers"),
    VARIABLE_NAME_INCORRECT_FORMAT(true, "variable name should be in camel case (correct: checkIpConfig, incorrect: CheckIPConfig)" +
            " should contain only latin (ASCII) letters or numbers and should start from lower letter"),
    VARIABLE_NAME_INCORRECT(false, "variable name should contain more than one letter"),
    CONSTANT_UPPERCASE(true, "<val> properties from companion object or on file level mostly in all cases are constants - please use upper snake case for them"),
    VARIABLE_HAS_PREFIX(true, "variable has prefix (like mVariable or M_VARIABLE), generally it is a bad code style (Android - is the only exception)"),
    IDENTIFIER_LENGTH(false, "identifier's length is incorrect, it should be in range of [2, 64] symbols"),
    ENUM_VALUE(true, "in the same way as constants, enum values should be in UPPER_CASE snake format"),
    GENERIC_NAME(true, "generic name should contain only one single capital letter, it can be followed by a number"),
    FUNCTION_NAME_INCORRECT_CASE(true, "function/method name should be in lowerCamelCase"),
    FUNCTION_BOOLEAN_PREFIX(true, "functions that return the value of Boolean type should have <is> or <has> prefix"),
    FILE_NAME_INCORRECT(true, "file name is incorrect - it should end with .kt extension and be in PascalCase"),
    FILE_NAME_MATCH_CLASS(true, "file name is incorrect - it should match with the class described in it if there is the only one class declared"),
    EXCEPTION_SUFFIX(true, "all exception classes should have \"Exception\" suffix"),

    // ======== chapter 2 ========
    MISSING_KDOC_TOP_LEVEL(false, "all public and internal top-level classes and functions should have Kdoc"),
    MISSING_KDOC_CLASS_ELEMENTS(false, "all public, internal and protected classes, functions and variables inside the class should have Kdoc"),
    MISSING_KDOC_ON_FUNCTION(true, "all public, internal and protected functions should have Kdoc with proper tags"),
    KDOC_TRIVIAL_KDOC_ON_FUNCTION(false, "KDocs should not be trivial (e.g. method getX should not de documented as 'returns X')"),
    KDOC_WITHOUT_PARAM_TAG(true, "all methods which take arguments should have @param tags in KDoc"),
    KDOC_WITHOUT_RETURN_TAG(true, "all methods which return values should have @return tag in KDoc"),
    KDOC_WITHOUT_THROWS_TAG(true, "all methods which throw exceptions should have @throws tag in KDoc"),
    BLANK_LINE_AFTER_KDOC(true, "there should be no empty line between Kdoc and code it is describing"),
    KDOC_EMPTY_KDOC(false, "KDoc should never be empty"),
    KDOC_WRONG_SPACES_AFTER_TAG(true, "there should be exactly one white space after tag name in KDoc"),
    KDOC_WRONG_TAGS_ORDER(true, "in KDoc standard tags are arranged in order @param, @return, @throws, but are"),
    KDOC_NEWLINES_BEFORE_BASIC_TAGS(true, "in KDoc block of standard tags @param, @return, @throws should contain newline before only if there is other content before it"),
    KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS(true, "in KDoc standard tags @param, @return, @throws should not containt newline between them, but these tags do"),
    KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS(true, "in KDoc there should be exactly one empty line after special tags"),
    KDOC_NO_EMPTY_TAGS(false, "no empty descriptions in tag blocks are allowed"),
    KDOC_NO_DEPRECATED_TAG(true, "KDoc doesn't support @deprecated tag, use @Deprecated annotation instead"),
    HEADER_WRONG_FORMAT(true, "file header comments should be properly formatted"),
    HEADER_MISSING_OR_WRONG_COPYRIGHT(true, "file header comment must include copyright information inside a block comment"),
    HEADER_CONTAINS_DATE_OR_AUTHOR(false, "file header comment should not contain creation date and author name"),
    HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE(false, "files that contain multiple or no classes should contain description of what is inside of this file"),
    HEADER_NOT_BEFORE_PACKAGE(true, "header KDoc should be placed before package and imports"),
    COMMENTED_OUT_CODE(false, "you should not comment out code, use VCS to save it in history and delete this block"),

    // ======== chapter 3 ========
    FILE_IS_TOO_LONG(false, "file has more number of lines than expected"),
    FILE_CONTAINS_ONLY_COMMENTS(false, "source code files which contain only comments should be avoided"),
    FILE_INCORRECT_BLOCKS_ORDER(true, "general structure of kotlin source file is wrong, parts are in incorrect order"),
    FILE_NO_BLANK_LINE_BETWEEN_BLOCKS(true, "general structure of kotlin source file is wrong, general code blocks sohuld be separated by empty lines"),
    FILE_UNORDERED_IMPORTS(true, "imports should be ordered alphabetically and shouldn't be separated by newlines"),
    FILE_WILDCARD_IMPORTS(false, "wildcard imports should not be used"),
    NO_BRACES_IN_CONDITIONALS_AND_LOOPS(true, "in if, else, when, for, do, and while statements braces should be used. Exception: single line if statement."),
    WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES(true, "the declaration part of a class-like code structures (class/interface/etc.) should be in the proper order"),
    BLANK_LINE_BETWEEN_PROPERTIES(true, "there should be no blank lines between properties without comments; comment or KDoc on property should have blank line before"),
    BRACES_BLOCK_STRUCTURE_ERROR(true, "braces should follow 1TBS style"),
    WRONG_INDENTATION(true, "only spaces are allowed for indentation and each indentation should equal to 4 spaces (tabs are not allowed)"),
    EMPTY_BLOCK_STRUCTURE_ERROR(true, "incorrect format of empty block"),
    MORE_THAN_ONE_STATEMENT_PER_LINE(true, "There should not be more than one code statement in one line"),
    LONG_LINE(true, "This line is longer than allowed"),
    BACKTICKS_PROHIBITED(false, "Backticks should not be used in identifier's naming. The only exception test methods marked with @Test annotation"),
    REDUNDANT_SEMICOLON(true, "there should be no redundant semicolon at the end of lines"),
    WRONG_NEWLINES(true, "incorrect line breaking"),
    // FixMe: autofixing will be added for this rule
    STRING_CONCATENATION(false, "strings should not be concatenated using plus operator - use string templates instead if the statement fits one line"),
    TOO_MANY_BLANK_LINES(true, "too many consecutive blank lines"),
    WRONG_WHITESPACE(true, "incorrect usage of whitespaces for code separation"),
    TOO_MANY_CONSECUTIVE_SPACES(true, "too many consecutive spaces"),
    ENUMS_SEPARATED(true, "split enumeration error"),
    ;

    override fun ruleName(): String = this.name

    fun warnText(): String = "[${ruleName()}] ${this.warn}:"

    @Suppress("LongParameterList")
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
