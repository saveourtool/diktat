package com.saveourtool.diktat.generation.docs

import java.io.File

/**
 * This function parses available-rules.md and rules-mapping.md files to automatically generate table for White paper
 */
@Suppress("MagicNumber", "UnsafeCallOnNullableType")
fun generateAvailableRules(rootDir: File, wpDir: File) {
    val ruleMap = File(rootDir, "rules-mapping.md").readLines()
        .drop(2)
        .map { it.drop(1).dropLast(1).split("|") }
        .map { RuleDescription(it[0].replace("\\s+".toRegex(), ""), it[1], it[2]) }
        .associateBy { it.ruleName }
    File(rootDir, "available-rules.md").readLines()
        .drop(2)
        .map { it.drop(1).dropLast(1).split("|") }
        .map { it[2].replace("\\s+".toRegex(), "") to it[5] }
        .forEach { ruleMap[it.first]!!.config = it.second}
    val newText = File(wpDir, "sections/appendix.tex").readLines().toMutableList()
    newText.removeAll(newText.subList(newText.indexOf("\\section*{Available Rules}") + 1, newText.indexOf("%CodeStyle")))
    var index = newText.indexOf("\\section*{Available Rules}") + 1
    AUTO_TABLE.trimIndent().lines().forEach { newText.add(index++, it) }
    ruleMap.map { it.value }
        .map { "${it.correctRuleName} & ${it.correctCodeStyle} & ${it.autoFix} & ${it.config.replace("<br>", " ")}\\\\" }
        .forEach { newText.add(index++, it) }
    AUTO_END.trimIndent().split("\n").forEach { newText.add(index++, it) }

    File(wpDir, "sections/appendix.tex").writeText(newText.joinToString(separator = "\n"))
}

/**
 * Data class for rule and it's description
 *
 * @property ruleName rule's name
 * @property codeStyle rule's description
 * @property autoFix is rule can fix
 */
@Suppress("UnsafeCallOnNullableType")
data class RuleDescription(val ruleName: String,
                           val codeStyle: String,
                           val autoFix: String) {
    /**
     * Remove square brackets from code style
     */
    val correctCodeStyle = codeStyle.substring(codeStyle.indexOf("[") + 1, codeStyle.indexOf("]")).run {
        "\\hyperref[sec:$this]{$this}"
    }

    /**
     * Replace space between words with underline for Latex
     */
    val correctRuleName = ruleName.replace("_", "\\underline{ }")

    /**
     * Parse correctly configuration for Latex
     */
    lateinit var config: String
}
