package com.saveourtool.diktat.ruleset.constants

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.ruleset.utils.isDigits
import org.jetbrains.kotlin.org.jline.utils.Levenshtein

/**
 * This class represents the chapters that are in our code style.
 *
 * @property number - number of chapter
 * @property title name of chapter
 */
@Suppress("WRONG_DECLARATIONS_ORDER")
enum class Chapters(val number: String, val title: String) {
    DUMMY("0", "Dummy"),
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
 * @param configRules list of rules configuration
 * @return is warning from enable chapter
 */
fun Warnings.isRuleFromActiveChapter(configRules: List<RulesConfig>): Boolean {
    val chapterFromRule = getChapterByWarning()
    val configuration = configRules.getCommonConfiguration()
    val disabledChapters = configuration.disabledChapters
        ?.takeIf { it.isNotBlank() }
        ?.split(",")
        ?.map { it.trim() }
        ?.mapNotNull { chap ->
            if (chap.isDigits()) {
                Chapters.values().find { chap == it.number }
            } else {
                validate(chap)
                Chapters.values().find { it.title == chap }
            }
        }
    return disabledChapters?.let { return chapterFromRule !in it } ?: true
}

/**
 * Function get chapter by warning
 *
 * @return chapter to which warning refers
 */
@Suppress("UnsafeCallOnNullableType")
fun Warnings.getChapterByWarning() = Chapters.values().find { it.number == this.ruleId.first().toString() }!!

private fun validate(chapter: String) =
    require(chapter in Chapters.values().map { it.title }) {
        val closestMatch = Chapters.values().minByOrNull { Levenshtein.distance(it.title, chapter) }
        "Chapter name <$chapter> in configuration file is invalid, did you mean <$closestMatch>?"
    }
