package org.cqfn.diktat.ruleset.constants

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.disabledChapters
import org.cqfn.diktat.ruleset.utils.isDigits
import org.jetbrains.kotlin.org.jline.utils.Levenshtein

/**
 * This class represents the chapters that are in our code style.
 *
 * @property number - number of chapter
 * @property chapterName name of chapter
 */
@Suppress("WRONG_DECLARATIONS_ORDER")
enum class Chapters(val number: String, val chapterName: String) {
    NAMING("1", "Naming"),
    COMMENTS("2", "Comments"),
    TYPESETTING("3", "General"),
    VARIABLES("4", "Variables"),
    FUNCTIONS("5", "Functions"),
    CLASSES("6", "Classes"),
    ;
}

/**
 * Function checks if warning from enable chapter
 *
 * @return is warning from enable chapter
 */
fun Warnings.isRuleFromActiveChapter(configRules: List<RulesConfig>): Boolean {
    val chapterFromRule = getChapterByWarning(this)
    val disabledChapters = configRules.disabledChapters()
        ?.takeIf { it.isNotBlank() }
        ?.split(",")
        ?.map { it.trim() }
        ?.mapNotNull { chap ->
            if (chap.isDigits()) {
                Chapters.values().find { chap == it.number }
            } else {
                validate(configRules)
                Chapters.values().find { it.chapterName == configRules.disabledChapters() }
            }
        }
    return disabledChapters?.let { return chapterFromRule !in it } ?: true
}

private fun validate(configRules: List<RulesConfig>) =
        require(configRules.disabledChapters() in Chapters.values().map {it.chapterName}) {
            val closestMatch = Chapters.values().minByOrNull { Levenshtein.distance(it.chapterName, configRules.disabledChapters()) }
            "Chapter name <${configRules.disabledChapters()}> in configuration file is invalid, did you mean <$closestMatch>?"
        }

/**
 * Function get chapter by warning
 *
 * @return chapter to which warning refers
 */
@Suppress("UnsafeCallOnNullableType")
fun getChapterByWarning(warnings: Warnings) = Chapters.values().find { it.number == warnings.ruleId.first().toString() }!!
