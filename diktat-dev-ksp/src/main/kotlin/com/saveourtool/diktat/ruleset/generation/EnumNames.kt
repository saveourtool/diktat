package com.saveourtool.diktat.ruleset.generation

/**
 * Annotation that marks to generate an object with names from Enum
 *
 * @property generatedPackageName
 * @property generatedClassName
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EnumNames(
    val generatedPackageName: String,
    val generatedClassName: String,
)
