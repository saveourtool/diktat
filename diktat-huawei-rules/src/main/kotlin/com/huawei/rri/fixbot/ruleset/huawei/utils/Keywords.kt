package com.huawei.rri.fixbot.ruleset.huawei.utils

import org.jetbrains.kotlin.lexer.KtTokens.KEYWORDS
import org.jetbrains.kotlin.lexer.KtTokens.SOFT_KEYWORDS

class Keywords {
    companion object {
        val JAVA = arrayOf("abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while")

        val KOTLIN = KEYWORDS.types.map { line -> line.toString() }
            .plus(SOFT_KEYWORDS.types.map { line -> line.toString() })

        fun isJavaKeyWord(word: String): Boolean = JAVA.contains(word)

        fun isKotlinKeyWord(word: String): Boolean = KOTLIN.contains(word)
    }
}
