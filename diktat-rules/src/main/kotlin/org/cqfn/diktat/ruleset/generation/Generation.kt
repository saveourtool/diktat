/**
 * This file contains code for codegen: generating a list of WarningNames and adjusting current year for (c)opyright inspection tests.
 */

package org.cqfn.diktat.ruleset.generation

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule.Companion.afterCopyrightRegex
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule.Companion.curYear
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule.Companion.hyphenRegex

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

import java.nio.file.Files
import java.nio.file.Paths

import kotlin.io.path.createTempFile
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

/**
 * The comment that will be added to the generated sources file.
 */
private val autoGenerationComment =
        """
            | This document was auto generated, please don't modify it.
            | This document contains all enum properties from Warnings.kt as Strings.
        """.trimMargin()

fun main() {
    generateWarningNames()
    validateYear()
}

private fun generateWarningNames() {
    val enumValNames = Warnings.values().map { it.name }

    val propertyList = enumValNames.map {
        PropertySpec
            .builder(it, String::class)
            .addModifiers(KModifier.CONST)
            .initializer("\"$it\"")
            .build()
    }

    val fileBody = TypeSpec
        .objectBuilder("WarningNames")
        .addProperties(propertyList)
        .build()

    val kotlinFile = FileSpec
        .builder("generated", "WarningNames")
        .addType(fileBody)
        .indent("    ")
        .addFileComment(autoGenerationComment)
        .build()

    kotlinFile.writeTo(Paths.get("diktat-rules/src/main/kotlin"))  // fixme: need to add it to pom
}

private fun validateYear() {
    val folder = Paths.get("diktat-rules/src/test/resources/test/paragraph2/header")
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
