package test.paragraph3.indentation

fun checkScript() {
    lintMethod(
                """
                    |val A = "ASD"
                            """.trimMargin(),
    )
}

fun lintMethod(trimMargin: String, fileName: String) {
    TODO("Not yet implemented")
}
