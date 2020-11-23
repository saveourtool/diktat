package cqfn.diktat.ruleset.constants

import org.cqfn.diktat.ruleset.constants.Warnings
import java.io.File

const val DIKTAT_GUIDE: String = "guide/diktat-coding-convention.md#"

@Suppress("MagicNumber")
fun main() {
    val allWarnings = Warnings.values()
    allWarnings.sortBy { warn ->
        val numbers = warn.ruleId.split(".")
        val chapter = numbers[0].toInt()
        val subChapter = numbers[1].toInt()
        val rule = numbers[2].toInt()

        // small hacky trick to compare rules like 1.1.13 properly (sorting using numbers instead of lexicographically)
        chapter * 100000 + subChapter * 100 + rule
    }

    val maxRuleIdLength = allWarnings.maxBy { it.ruleId.length }?.ruleId?.length ?: 0
    val maxRuleNameLength = allWarnings.maxBy { it.name.length }?.name?.length ?: 0
    val separator = "| ${"-".repeat(maxRuleNameLength)} | ${"-".repeat(maxRuleIdLength)} | --- |\n"

    val header = "| Diktat Rule | Code Style | Auto-fixed? |\n"

    val tableWithWarnings = allWarnings.map { warn ->
        "| ${warn.name} | [${warn.ruleId}](${DIKTAT_GUIDE}r${warn.ruleId}) | ${if (warn.canBeAutoCorrected) "yes" else "no"} |"
    }.joinToString("\n")

    File("info/rules-mapping.md").writeText("$header$separator$tableWithWarnings")
}
