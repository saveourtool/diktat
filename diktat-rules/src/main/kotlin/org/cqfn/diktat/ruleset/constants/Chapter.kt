package org.cqfn.diktat.ruleset.constants

import org.cqfn.diktat.ruleset.utils.isDigits
import org.jetbrains.kotlin.org.jline.utils.Levenshtein
import java.lang.Character.getNumericValue

enum class Chapters(val number: Int, val chapterName: String) {
    NAMING(1, "Naming"),
    COMMENTS(2, "Comments"),
    TYPESETTING(3, "General formatting (typesetting)"),
    VARIABLES(4, "Variables and types"),
    FUNCTIONS(5, "Functions"),
    CLASSES(6, "Classes, interfaces, and extension functions"),
    ;
}

fun isRuleFromActiveChapter(disabled: String?, warnings: Warnings): Boolean {
    val chapterFromRule = getChapterByWarning(warnings)
    val disabledChapters = disabled
        ?.takeIf { it.isNotBlank() }
        ?.split(",")
        ?.map { it.trim() }
        ?.mapNotNull { chap ->
            if (chap.isDigits()) {
                Chapters.values().find { chap.toInt() == it.number }
            } else {
                Chapters.values().minByOrNull { Levenshtein.distance(it.chapterName, disabled) }
            }
        }
    return disabledChapters?.let { return chapterFromRule !in it } ?: true
}

@Suppress("UnsafeCallOnNullableType")
fun getChapterByWarning(warnings: Warnings) = Chapters.values().find { it.number == getNumericValue(warnings.ruleId.first()) }!!
