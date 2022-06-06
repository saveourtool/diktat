package test.paragraph3.indentation

fun checkScript() {
    lintMethod(
        """
                |val q = 1
                |
            """.trimMargin(),
        fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}

fun lintMethod(trimMargin: String, fileName: String) {
    TODO("Not yet implemented")
}
