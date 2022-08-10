package org.cqfn.diktat.ruleset.chapter3.spaces.junit

import generated.WarningNames.WRONG_INDENTATION
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * @property includeWarnTests whether unit tests for the "warn" mode should also
 *   be generated. If `false`, only fix mode tests get generated. The default is
 *   `true`.
 */
@Target(FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
@TestTemplate
@ExtendWith(IndentationTestInvocationContextProvider::class)
@Tag(WRONG_INDENTATION)
annotation class IndentationTest(
    val first: IndentedSourceCode,
    val second: IndentedSourceCode,
    val includeWarnTests: Boolean = true
)
