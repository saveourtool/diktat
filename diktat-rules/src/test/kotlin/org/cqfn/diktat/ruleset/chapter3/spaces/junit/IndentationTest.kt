package org.cqfn.diktat.ruleset.chapter3.spaces.junit

import generated.WarningNames.WRONG_INDENTATION
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(
    ANNOTATION_CLASS,
    FUNCTION,
)
@Retention(RUNTIME)
@MustBeDocumented
@TestTemplate
@ExtendWith(IndentationTestInvocationContextProvider::class)
@Tag(WRONG_INDENTATION)
annotation class IndentationTest(
    val first: IndentedSourceCode,
    val second: IndentedSourceCode,
)
