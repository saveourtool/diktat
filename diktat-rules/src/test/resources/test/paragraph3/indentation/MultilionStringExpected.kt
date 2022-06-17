package test.paragraph3.indentation

//test of correct opening quotation mark and incorrect closing quotation mark
fun multilionString() {
    lintMethod(
            """
                |val q = 1
                |
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}

//test of incorrect opening quotation mark and incorrect closing quotation mark1
fun multilionString() {
    lintMethod(
            """
                    |val q = 1
                    |
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}

//test of incorrect opening quotation mark and incorrect closing quotation mark2
fun multilionString() {
    lintMethod(
            """
                    |val q = 1
                    |
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}

//test of incorrect opening quotation mark and incorrect closing quotation mark with incorrect shift
fun multilionString() {
    lintMethod(
            """
                    |val q = 1
                    |
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
    )
}

