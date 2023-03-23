/**
 * This file contains code for codegen: generating a list of WarningNames and adjusting current year for (c)opyright inspection tests.
 */

package org.cqfn.diktat.ruleset.generation

import org.cqfn.diktat.ruleset.constants.Warnings

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

import java.nio.file.Paths

/**
 * The comment that will be added to the generated sources file.
 */
private val autoGenerationComment =
    """
        | This document was auto generated, please don't modify it.
        | This document contains all enum properties from Warnings.kt as Strings.
    """.trimMargin()

fun main(args: Array<String>) {
    require(args.size == 1) {
        "Expected only one argument: <source root>"
    }
    generateWarningNames(args[0])
}

private fun generateWarningNames(sourceDirectory: String) {
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

    kotlinFile.writeTo(Paths.get(sourceDirectory))
}
