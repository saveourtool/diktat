package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import generated.WarningNames.WRONG_INDENTATION
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * @property includeWarnTests whether unit tests for the "warn" mode should also
 *   be generated. If `false`, the code is allowed to have no expected-error
 *   annotations, and only fix mode tests get generated. The default is `true`.
 * @property singleConfiguration whether only a single code fragment is to be
 *   analysed. If `true`, the value of [second] is ignored, resulting in fewer
 *   unit tests being generated. The default is `false`.
 */
@Target(FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
@TestTemplate
@ExtendWith(IndentationTestInvocationContextProvider::class)
@Tag(WRONG_INDENTATION)
annotation class IndentationTest(
    val first: IndentedSourceCode,
    val second: IndentedSourceCode = IndentedSourceCode(""),
    val includeWarnTests: Boolean = true,
    val singleConfiguration: Boolean = false,
)
