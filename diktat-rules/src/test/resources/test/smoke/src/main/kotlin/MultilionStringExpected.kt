package org.cqfn.diktat

fun checkScript() {
    lintMethod(
        """
            |val q = 1
            |
        """.trimMargin(),
        fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}
