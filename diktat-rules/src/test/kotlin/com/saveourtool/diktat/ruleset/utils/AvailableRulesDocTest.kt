package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.ruleset.constants.Warnings
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Special test to check that developer has not forgotten to write documentation for each warning.
 * Documentation should be added to available-rules.md
 */
class AvailableRulesDocTest {
    private fun getAllRulesFromDoc(): List<String> {
        val listWithRulesFromDoc: MutableList<String> = mutableListOf()
        File(AVAILABLE_RULES_FILE).forEachLine { line ->
            val splitMarkDown = line
                .split("|")

            val ruleName = splitMarkDown[SPLIT_MARK].trim()

            if (!ruleName.startsWith(TABLE_DELIMITER) &&
                    !ruleName.startsWith(RULE_NAME_HEADER)) {
                listWithRulesFromDoc.add(ruleName)
            }
        }
        return listWithRulesFromDoc
    }

    @Test
    fun `read rules from documentation`() {
        val allRulesFromCode = Warnings.values().filterNot { it == Warnings.DUMMY_TEST_WARNING }
        val allRulesFromDoc = getAllRulesFromDoc()

        allRulesFromCode.forEach { warning ->
            val ruleName = warning.ruleName()
            val ruleFound = allRulesFromDoc.any { it.trim() == ruleName }
            Assertions.assertTrue(ruleFound) {
                val docs = "| | | $ruleName" +
                        "|  |  |  | |"
                """
                    Cannot find warning $ruleName in $AVAILABLE_RULES_FILE.
                    You can fix it by adding the following description below with more info to $AVAILABLE_RULES_FILE:
                    add $docs to $AVAILABLE_RULES_FILE
                """
            }
        }

        allRulesFromDoc.forEach { warning ->
            val trimmedWarning = warning.trim()
            val ruleFound = allRulesFromCode.any { it.ruleName() == trimmedWarning }
            Assertions.assertTrue(ruleFound) {
                """
                    Found rule (warning) in documentation: <$trimmedWarning> that does not exist in the code. Misprint or configuration was renamed?
                """.trimIndent()
            }
        }
    }

    companion object {
        const val AVAILABLE_RULES_FILE = "../info/available-rules.md"
        const val RULE_NAME_HEADER = "Rule name"
        const val SPLIT_MARK = 3
        const val TABLE_DELIMITER = "-----"
    }
}
