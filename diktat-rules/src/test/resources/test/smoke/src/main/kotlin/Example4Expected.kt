package org.cqfn.diktat

class SpecialTagsInKdoc {
    /**
     * Empty function to test KDocs
     * @apiNote foo
     *
     * @implSpec bar
     *
     * @implNote baz
     *
     * @return
     */
    fun test() = Unit
}

fun `method name incorrect, part 4`() {
    val code = """
                  class TestPackageName {
                    fun methODTREE(): String {
                    }
                  }
                """.trimIndent()
    lintMethod(code, LintError(2, 7, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true))

    foo
        // we are calling bar
        .bar()

    bar
        /* This is a block comment */
        .foo()
}

fun foo() {
    foo(
        0,
        { obj -> obj.bar() }
    )

    bar(
        0, { obj -> obj.bar() }
    )
}

fun bar() {
    val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java).apply {
        inputs = project.fileTree("src").apply {
            include("**/*.kt")
        }
        reporter = PlainReporter(System.out)
    }
}

@Suppress("")
fun foo() {
    val y = "akgjsaujtmaksdkfasakgjsaujtmaksdkfasakgjsaujtmaksdkfasakgjsaujtm" +
            " aksdkfasfasakgjsaujtmaksdfasafasakgjsaujtmaksdfasakgjsaujtmaksdfasakgjsaujtmaksdfasakgjsaujtmaksdfasakgjsaujtmaksdkgjsaujtmaksdfasakgjsaujtmaksd"
}

