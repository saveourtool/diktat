package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.constants.Warnings
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Special test to check that developer has not forgotten to write documentation for each warning.
 * Documentation should be added to available-rules.md
 */
class AvailableRulesDocTest {
    companion object {
        const val availableRulesFile = "../info/available-rules.md"
        const val tableDelimiter = "-----"
        const val ruleNameHeader = "Rule name"
    }

    @Test
    fun `read rules from documentation`() {
        val allRulesFromCode = Warnings.values()
        val allRulesFromDoc = getAllRulesFromDoc()

        allRulesFromCode.forEach { warning ->
            val ruleName = warning.ruleName()
            val ruleFound = allRulesFromDoc.find { it.trim() == ruleName } != null
            require(ruleFound) {
                val docs = "| | | $ruleName" +
                        "|  |  |  | |"
                """
                    Cannot find warning $ruleName in $availableRulesFile.
                    You can fix it by adding the following description below with more info to $availableRulesFile:
                    add $docs to $availableRulesFile
                """
            }
        }

        allRulesFromDoc.forEach { warning ->
            val trimmedWarning = warning.trim()
            val ruleFound = allRulesFromCode.find { it.ruleName() == trimmedWarning } != null
            require(ruleFound) {
                """
                    Found rule (warning) in documentation: <$trimmedWarning> that does not exist in the code. Misprint or configuration was renamed? 
                """.trimIndent()
            }
        }
    }

    private fun getAllRulesFromDoc(): List<String> {
        val listWithRulesFromDoc = mutableListOf<String>()
        File(availableRulesFile).forEachLine { line ->
            val splitMarkDown = line
                    .split("|")

            val ruleName = splitMarkDown.get(3).trim()

            if (!ruleName.startsWith(tableDelimiter) &&
                    !ruleName.startsWith(ruleNameHeader)) {
                listWithRulesFromDoc.add(ruleName)
            }
        }
        return listWithRulesFromDoc
    }
}
