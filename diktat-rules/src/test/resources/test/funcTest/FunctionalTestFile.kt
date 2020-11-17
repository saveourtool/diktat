package test.funcTest

class FunctionalTestFile {
    val AAAA = 5

    fun `method name incorrect, part 4`() {
        val code = """
                  class TestPackageName {
                    fun methODTREE(): String {
                    }
                  }
                """.trimIndent()
        lintMethod(code, LintError(2, 7, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true))
    }
}