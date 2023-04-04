@file:Suppress("FILE_NAME_MATCH_CLASS")

package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE

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
