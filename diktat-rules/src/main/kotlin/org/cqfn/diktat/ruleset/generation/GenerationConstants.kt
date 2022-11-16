/**
 * This file contains code for codegen: generating a file with diktat constants
 */

package org.cqfn.diktat.ruleset.generation

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

import java.nio.file.Paths

import kotlin.io.path.readText


fun main(args: Array<String>) {
    require(args.size == 3) {
        "Only four arguments are expected: <source root> <diktat version> <license file>"
    }
    generateDiktatConstants(args[0], args[1], args[2])
}

private fun generateDiktatConstants(
    sourceDirectory: String,
    version: String,
    licenseFile: String,
) {
    FileSpec
        .builder("generated", "DiktatConstants")
        .addProperty(
            PropertySpec.builder("DIKTAT_VERSION", String::class)
                .addModifiers(KModifier.CONST)
                .addModifiers(KModifier.INTERNAL)
                .initializer("%S", version)
                .build()
        )
        .addProperty(
            PropertySpec.builder("LICENSE", String::class)
                .addModifiers(KModifier.CONST)
                .addModifiers(KModifier.INTERNAL)
                .initializer("%S", Paths.get(licenseFile).readText())
                .build()
        )
        .indent("    ")
        .addFileComment("""
            | $autoGenerationComment.
            | This document contains diktat constants.
        """.trimMargin())
        .build()
        .writeTo(Paths.get(sourceDirectory))
}
