package org.cqfn.diktat.ruleset.generation

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File


private const val AUTO_GENERATION_COMMENT =
        " This document was auto generated, please dont modify it\n" +
        " This document contains all enum properties from Warnings.kt as Strings"

fun main(){
    val bufferedReader = File("src/main/kotlin/org/cqfn/diktat/ruleset/constants/Warnings.kt").bufferedReader()
    val lineList = mutableListOf<String>()

    bufferedReader.useLines { lines ->
        val stringSeq = lines.filter { it.contains(Regex("([A-Z_]{5,}\\()")) }

        stringSeq.forEach {
            val enumConstName = it.
                    replaceAfter("(", "").
                    dropLast(1).
                    trim()
            lineList.add(enumConstName) }
    }

    val propertyList = mutableListOf<PropertySpec>()

    lineList.forEach {
        val prop = PropertySpec.builder(it, String::class)
                .addModifiers(KModifier.CONST)
                .initializer("\"$it\"")
                .build()

        propertyList.add(prop)
    }
    val fileBody = TypeSpec.objectBuilder("WarningNames")
            .addProperties(propertyList)
            .build()

    val kotlinFile = FileSpec.builder("generated", "WarningNames")
            .addType(fileBody)
            .addComment(AUTO_GENERATION_COMMENT)
            .build()

    kotlinFile.writeTo(File("src/main/kotlin"))

}
