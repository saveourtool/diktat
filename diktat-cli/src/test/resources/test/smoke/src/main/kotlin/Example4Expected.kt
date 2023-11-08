// ;warn:$line:1: [FILE_NAME_MATCH_CLASS] file name is incorrect - it should match with the class described in it if there is the only one class declared: Example4Expected.kt vs SpecialTagsInKdoc{{.*}}
package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: SpecialTagsInKdoc (cannot be auto-corrected){{.*}}
class SpecialTagsInKdoc {
    /**
     * Empty function to test KDocs
     * @apiNote foo
     *
     * @implSpec bar
     *
     * @implNote baz
     *
     // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun test() = Unit
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: `method name incorrect, part 4` (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: `method name incorrect, part 4` (cannot be auto-corrected){{.*}}
// ;warn:5: [BACKTICKS_PROHIBITED] backticks should not be used in identifier's naming. The only exception test methods marked with @Test annotation: `method name incorrect, part 4` (cannot be auto-corrected){{.*}}
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

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: foo (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: foo (cannot be auto-corrected){{.*}}
fun foo() {
    foo(
        0,
        { obj -> obj.bar() }
    )

    bar(
        0, { obj -> obj.bar() }
    )
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: bar (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: bar (cannot be auto-corrected){{.*}}
fun bar() {
    val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java).apply {
        inputs = project.fileTree("src").apply {
            include("**/*.kt")
        }
        reporter = PlainReporter(System.out)
    }
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: boo (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: boo (cannot be auto-corrected){{.*}}
@Suppress("")
fun boo() {
    val y = "akgjsaujtmaksdkfasakgjsaujtmaksdkfasakgjsaujtmaksdkfasakgjsaujtm" +
            " aksdkfasfasakgjsaujtmaksdfasafasakgjsaujtmaksdfasakgjsaujtmaksdfasakgjsaujtmaksdfasakgjsaujtmaksdfasakgjsaujtmaksdkgjsaujtmaksdfasakgjsaujtmaksd"
}
