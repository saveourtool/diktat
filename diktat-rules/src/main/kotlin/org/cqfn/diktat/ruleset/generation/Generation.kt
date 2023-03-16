/**
 * This file contains code for codegen: generating a list of WarningNames and adjusting current year for (c)opyright inspection tests.
 */

package org.cqfn.diktat.ruleset.generation

import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule.Companion.afterCopyrightRegex
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule.Companion.curYear
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule.Companion.hyphenRegex

import java.nio.file.Files
import java.nio.file.Paths

import kotlin.io.path.createTempFile
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

fun main(args: Array<String>) {
    require(args.size == 2) {
        "Only two arguments are expected: <source root> <test resource root>"
    }
    validateYear(args[1])
}

private fun validateYear(testResourcesDirectory: String) {
    val folder = Paths.get(testResourcesDirectory, "test/paragraph2/header")
    Files.list(folder)
        .filter { !it.name.contains("CopyrightDifferentYearTest.kt") }
        .forEach { file ->
            val tempFile = createTempFile()
            tempFile.writeLines(file.readLines()
                .map { line ->
                    when {
                        line.contains(hyphenRegex) -> line.replace(hyphenRegex) {
                            val years = it.value.split("-")
                            "${years[0]}-$curYear"
                        }
                        line.contains(afterCopyrightRegex) -> line.replace(afterCopyrightRegex) {
                            val copyrightYears = it.value.split("(c)", "(C)", "©")
                            "${copyrightYears[0]}-$curYear"
                        }
                        else -> line
                    }
                })
            Files.delete(file)
            Files.move(tempFile, file)
        }
}
