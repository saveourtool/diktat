package org.cqfn.diktat

fun checkScript() {
    lintMethod(
            """
                        |val A = "aa"
                    """.trimMargin(),
    )
}

fun checkScript() {
    lintMethod(
        """
                    |val q = 1
                    |
                """.trimMargin(),
        fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}

fun `check dummy property`() {
    lintMethod(
        """
                                    |// provide your check here
                        """.trimMargin(),
                        LintError(1, 1, ruleId, "${DUMMY_TEST_WARNING.warnText()} some detailed explanation", true)
    )
}
