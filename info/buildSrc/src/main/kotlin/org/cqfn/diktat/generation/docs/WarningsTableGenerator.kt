package org.cqfn.diktat.generation.docs

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.getChapterByWarning

import java.io.File

/**
 * Path to diktat code style guide, which has links to code style chapters
 */
const val DIKTAT_GUIDE: String = "guide/diktat-coding-convention.md#"

/**
 * Generates a table, which maps warning names to chapters in code style
 */
@Suppress("MagicNumber")
fun generateRulesMapping() {
    val allWarnings = Warnings.values()
    allWarnings.sortBy { warn ->
        val numbers = warn.ruleId.split(".")
        val chapter = numbers[0].toInt()
        val subChapter = numbers[1].toInt()
        val rule = numbers[2].toInt()

        // small hacky trick to compare rules like 1.1.13 properly (sorting using numbers instead of lexicographically)
        chapter * 100_000 + subChapter * 100 + rule
    }

    val maxRuleIdLength = allWarnings
        .maxBy { it.ruleId.length }
        ?.ruleId
        ?.length
        ?: 0
    val maxRuleNameLength = allWarnings
        .maxBy { it.name.length }
        ?.name
        ?.length
        ?: 0

    val tableWithWarnings = allWarnings.joinToString("\n") { warn ->
        "| ${warn.name} | [${warn.ruleId}](${DIKTAT_GUIDE}r${warn.ruleId}) | ${if (warn.canBeAutoCorrected) "yes" else "no"} | ${warn.getChapterByWarning().title} |"
    }

    val chaptersLength = allWarnings.map { it.getChapterByWarning().title }.maxBy { it.length }?.length ?: 0

    val header = "| Diktat Rule | Code Style | Auto-fixed? | Chapter |\n"
    val separator = "| ${"-".repeat(maxRuleNameLength)} | ${"-".repeat(maxRuleIdLength)} | --- | ${"-".repeat(chaptersLength)} |\n"
    File("rules-mapping.md").writeText("$header$separator$tableWithWarnings")
}
