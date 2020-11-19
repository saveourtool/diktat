package cqfn.diktat.ruleset.constants

import org.cqfn.diktat.ruleset.constants.Warnings
import java.io.File

fun main() {
    val allWarnings = Warnings.values()
    allWarnings.sortBy { it.ruleId }

    val maxRuleIdLength = allWarnings.maxBy { it.ruleId.length }?.ruleId?.length
    val maxRuleNameLength = allWarnings.maxBy { it.name.length }?.name?.length
    val separator = "| ${"-".repeat(maxRuleNameLength ?: 0)} | ${"-".repeat(maxRuleIdLength ?: 0)} |\n"

    val header = "| Diktat Rule | Code Style |\n"

    val tableWithWarnings = allWarnings.map { warn ->
        "| ${warn.name} | [${warn.ruleId}](diktat-coding-convention.md#${warn.ruleId}) |"
    }.joinToString("\n")

    File("info/rules-mapping.md").writeText("$header$separator$tableWithWarnings")
}
