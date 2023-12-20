package com.saveourtool.diktat.ruleset.generation

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * [SymbolProcessor] to generate a class with contacts for names from provided enum
 *
 * @param codeGenerator
 */
class EnumNamesSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getEnumDeclarations().forEach { doProcess(resolver, it) }
        return emptyList()
    }

    private fun doProcess(resolver: Resolver, enumDeclaration: KSClassDeclaration) {
        val annotation = enumDeclaration.annotations
            .single {
                it.shortName.asString() == EnumNames::class.simpleName
            }
        val targetPackageName = annotation.getArgumentValue("generatedPackageName")
        val targetClassName = annotation.getArgumentValue("generatedClassName")
        if (resolver.isAlreadyGenerated(targetPackageName, targetClassName)) {
            return
        }
        codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = targetPackageName,
            fileName = targetClassName,
        ).bufferedWriter()
            .use { writer ->
                writer.write(autoGenerationComment)
                writer.newLine()
                writer.write("package $targetPackageName\n")
                writer.newLine()
                writer.write("import kotlin.String\n")
                writer.newLine()
                writer.write("object $targetClassName {\n")
                enumDeclaration.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .map { it.simpleName.asString() }
                    .forEach { enumEntryName ->
                        writer.write("    const val $enumEntryName: String = \"$enumEntryName\"\n")
                    }
                writer.write("}\n")
            }
    }

    companion object {
        /**
         * The comment that will be added to the generated sources file.
         */
        private val autoGenerationComment =
            """
                |/**
                | * This document was auto generated, please don't modify it.
                | * This document contains all enum properties from Warnings.kt as Strings.
                | */
            """.trimMargin()
        private val annotationName: String = requireNotNull(EnumNames::class.qualifiedName) {
            "Failed to retrieve a qualified name from ${EnumNames::class}"
        }

        private fun Resolver.getEnumDeclarations(): Sequence<KSClassDeclaration> = getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .onEach { candidate ->
                require(candidate.classKind == ClassKind.ENUM_CLASS) {
                    "Annotated class ${candidate.qualifiedName} is not enum"
                }
            }

        private fun KSAnnotation.getArgumentValue(argumentName: String): String = arguments
            .singleOrNull { it.name?.asString() == argumentName }
            .let { argument ->
                requireNotNull(argument) {
                    "Not found $argumentName in $this"
                }
            }
            .value
            ?.let { it as? String }
            .let { argumentValue ->
                requireNotNull(argumentValue) {
                    "Not found a value for $argumentName in $this"
                }
            }

        private fun Resolver.isAlreadyGenerated(
            packageName: String,
            className: String,
        ): Boolean = getNewFiles()
            .find { it.packageName.asString() == packageName && it.fileName == "$className.kt" }
            ?.let { true }
            ?: false
    }
}
