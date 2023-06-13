@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.saveourtool.diktat.ruleset.utils

import org.jetbrains.kotlin.KtNodeTypes.DO_WHILE
import org.jetbrains.kotlin.KtNodeTypes.FOR
import org.jetbrains.kotlin.KtNodeTypes.WHILE
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.SEMICOLON
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE

internal const val GET_PREFIX = "get"
internal const val SET_PREFIX = "set"
internal const val EMPTY_BLOCK_TEXT = "{}"

/**
 * List of standard methods which do not need mandatory documentation
 */
internal val standardMethods = listOf("main", "equals", "hashCode", "toString", "clone", "finalize")

/**
 * Mapping (value is negative infix) of infix methods that return Boolean
 */
internal val logicalInfixMethodMapping = mapOf(
    "==" to "!=",
    "!=" to "==",
    ">" to "<=",
    "<" to ">=",
    ">=" to "<",
    "<=" to ">",
    "in" to "!in",
    "!in" to "in",
)

/**
 * List of infix methods that return Boolean
 */
internal val logicalInfixMethods = logicalInfixMethodMapping.keys + "xor"

/**
 * List of element types present in empty code block `{ }`
 */
val emptyBlockList = listOf(LBRACE, WHITE_SPACE, SEMICOLON, RBRACE)

val commentType = listOf(BLOCK_COMMENT, EOL_COMMENT, KDOC)
val loopType = listOf(FOR, WHILE, DO_WHILE)
val copyrightWords = setOf("copyright", "版权")

internal val operatorMap = mapOf(
    "unaryPlus" to "+", "unaryMinus" to "-", "not" to "!",
    "plus" to "+", "minus" to "-", "times" to "*", "div" to "/", "rem" to "%", "mod" to "%", "rangeTo" to "..",
    "inc" to "++", "dec" to "--", "contains" to "in",
    "plusAssign" to "+=", "minusAssign" to "-=", "timesAssign" to "*=", "divAssign" to "/=", "modAssign" to "%=",
).mapValues { (_, value) ->
    listOf(value)
} + mapOf(
    "equals" to listOf("==", "!="),
    "compareTo" to listOf("<", "<=", ">", ">="),
)

internal val ignoreImports = setOf("invoke", "get", "set", "getValue", "setValue", "provideDelegate")

/**
 * Enum that represents some standard platforms that can appear in kotlin code
 * @property packages beginnings of fully qualified names of packages belonging to a particular platform
 */
enum class StandardPlatforms(val packages: List<String>) {
    ANDROID(listOf("android", "androidx", "com.android")),
    JAVA(listOf("java", "javax")),
    KOTLIN(listOf("kotlin", "kotlinx")),
    ;
}
