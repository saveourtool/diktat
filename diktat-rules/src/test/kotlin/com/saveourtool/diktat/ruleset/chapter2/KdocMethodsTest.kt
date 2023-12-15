package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_TRIVIAL_KDOC_ON_FUNCTION
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class KdocMethodsTest : LintTestBase(::KdocMethods) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}"
    private val funCode = """
        fun doubleInt(a: Int): Int {
            if (Config.condition) throw IllegalStateException()
            return 2 * a
        }
    """.trimIndent()

    @Test
    @Tags(
        Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG),
        Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG),
        Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    )
    fun `Accessible methods with parameters, return type and throws should have proper KDoc (positive example)`() {
        val validCode = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
            $funCode
        """.trimIndent()

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `Warning should not be triggered for private functions`() {
        val validCode = "private $funCode"

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `anonymous function`() {
        val code = """
            package com.saveourtool.diktat.test

            fun foo() {
                val sum: (Int) -> Int = fun(x): Int = x + x
            }

        """.trimIndent()
        lintMethod(code,
            DiktatError(3, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
        )
    }

    @Test
    @Tags(
        Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG),
        Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG),
        Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG),
        Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    )
    fun `Warning should not be triggered for functions in tests`(@TempDir tempDir: Path) {
        val validCode = "@Test $funCode"
        val complexAnnotationCode = "@Anno(test = [\"args\"]) $funCode"

        // do not force KDoc on annotated function
        lintMethodWithFile(validCode, tempDir = tempDir, fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kt")
        // no false positive triggers on annotations
        lintMethodWithFile(complexAnnotationCode,
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kt",
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} doubleInt", true)
        )
        // should check all .kt files unless both conditions on location and name are true
        lintMethodWithFile(funCode,
            tempDir = tempDir,
            fileName = "src/test/kotlin/com/saveourtool/diktat/Example.kt",
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} doubleInt", true)
        )
        // should allow to set custom test dirs
        lintMethodWithFile(funCode,
            tempDir = tempDir,
            fileName = "src/jvmTest/kotlin/com/saveourtool/diktat/ExampleTest.kt",
            rulesConfigList = listOf(RulesConfig(DIKTAT_COMMON, true, mapOf("testDirs" to "test,jvmTest")))
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `Empty parameter list should not trigger warning about @param`() {
        val validCode = """
            /**
             * Test method
             * @return zero
             * @throws IllegalStateException
             */
            fun foo(): Int {
                return 0
            }
        """.trimIndent()

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `All methods with parameters should have @param KDoc`() {
        val invalidKdoc = """
            /**
             * Test method
             * @return doubled value
             * @throws IllegalStateException
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(1, 13, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} doubleInt (a)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `All methods with parameters should have @param KDoc for each parameter`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            fun addInts(a: Int, b: Int): Int = a + b
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(1, 12, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} addInts (b)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG)
    fun `All methods with explicit return type excluding Unit should have @return KDoc`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @throws IllegalStateException
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(1, 13, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} doubleInt", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG)
    fun `All methods with expression body should have @return tag or explicitly set return type to Unit`() {
        val kdocWithoutReturn = """
            /**
             * Test method
             * @param a - dummy integer
             * @throws IllegalStateException
             */
        """.trimIndent()

        val invalidCode = """
            $kdocWithoutReturn
            fun foo(a: Int) = bar(2 * a)

            $kdocWithoutReturn
            fun bar(a: Int): Unit = this.list.add(a)

            private val list = mutableListOf<Int>()
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(1, 12, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} foo", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `All methods with throw in method body should have @throws KDoc`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(1, 13, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalStateException)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `Linter shouldn't detect throws inside comments`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            fun foo(a: Int) {
                // throw Exception()
                return bar
            }
        """.trimIndent()

        lintMethod(invalidCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `All thrown exceptions should be in KDoc`() {
        val invalidCode = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
            fun doubleInt(a: Int): Int {
                if (Config.condition) throw IllegalStateException()
                if (Config.condition2) throw IllegalAccessException()
                return 2 * a
            }
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(1, 1, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalAccessException)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `No warning when throw has matching catch`() {
        lintMethod(
            """
                    /**
                      * Test method
                      * @param a: Int - dummy integer
                      * @return doubled value
                      */
                    fun foo(a: Int): Int {
                        try {
                            if (a < 0)
                                throw NumberFormatExecption()
                        } catch (e: ArrayIndexOutOfBounds) {
                            print(1)
                        } catch (e: NullPointerException) {
                            print(2)
                        } catch (e: NumberFormatExecption) {
                            print(3)
                        }
                        return 2 * a
                    }
            """.trimIndent())
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `Warning when throw doesn't have matching catch`() {
        lintMethod(
            """
                    /**
                      * Test method
                      * @param a: Int - dummy integer
                      * @return doubled value
                      */
                    fun foo(a: Int): Int {
                        try {
                            if (a < 0)
                                throw NumberFormatException()
                        throw NullPointerException()
                        throw NoSuchElementException()
                        } catch (e: NoSuchElementException) {
                            print(1)
                        } catch (e: IllegalArgumentException) {
                            print(2)
                        }
                        return 2 * a
                    }
            """.trimIndent(),
        DiktatError(1, 1, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} foo (NullPointerException)", true))
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `No warning when throw has matching catch, which is parent exception to throw`() {
        lintMethod(
            """
                    /**
                      * Test method
                      * @param a: Int - dummy integer
                      * @return doubled value
                      */
                    fun foo(a: Int): Int {
                        try {
                            if (a < 0)
                                throw NumberFormatException()
                        } catch (e: IllegalArgumentException) {
                            print(1)
                        }
                        return 2 * a
                    }
            """.trimIndent())
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `do not force documentation on standard methods`() {
        lintMethod(
            """
                    |class Example {
                    |    override fun toString() = "example"
                    |
                    |    override fun equals(other: Any?) = false
                    |
                    |    override fun hashCode() = 42
                    |}
                    |
                    |fun main() { }
            """.trimMargin()
        )

        lintMethod(
            """
                    |class Example {
                    |    override fun toString(): String { return "example" }
                    |
                    |    override fun equals(other: Any?): Boolean { return false }
                    |
                    |    override fun hashCode(): Int { return 42 }
                    |}
                    |
                    |fun main(vararg args: String) { }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `should not force documentation on single line getters and setters`() {
        lintMethod(
            """
                    |class Example {
                    |    fun setX(x: Type) {
                    |        this.x = x
                    |    }
                    |
                    |    fun getX(): Type {
                    |        return x
                    |    }
                    |
                    |    fun getY() = this.y
                    |
                    |    fun setY(y: Type) {
                    |        this.validate(y)
                    |        this.y = y
                    |    }
                    |
                    |    fun getZ(): TypeZ {
                    |        baz(z)
                    |        return z
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(12, 5, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} setY", true),
            DiktatError(17, 5, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} getZ", true)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `regression - warn about missing KDoc even if it cannot be autocorrected`() {
        lintMethod(
            """
                    |fun foo() { }
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_TRIVIAL_KDOC_ON_FUNCTION)
    fun `should check if KDoc is not trivial`() {
        lintMethod(
            """
                    |/**
                    | * Returns X
                    | */
                    |fun getX(): TypeX { return x }
            """.trimMargin(),
            DiktatError(2, 3, ruleId, "${KDOC_TRIVIAL_KDOC_ON_FUNCTION.warnText()} Returns X", false)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `should not trigger on override funcs`() {
        lintMethod(
            """
                    |class Some : A {
                    |   override fun foo() {}
                    |
                    |   override fun bar(t: T): U { return U() }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `should check if KfDoc is not trivial`() {
        lintMethod(
            """
                    |fun foo(x: Int): TypeX {
                    |   val q = goo()
                    |   throw UnsupportedOperationException()
                    |   return qwe
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", true)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc should be for function with single line body`() {
        lintMethod(
            """
                    |fun hasNoChildren() = children.size == 0
                    |fun getFirstChild() = children.elementAtOrNull(0)
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} hasNoChildren", true),
            DiktatError(2, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} getFirstChild", true)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc shouldn't be for function with name as method`() {
        lintMethod(
            """
                    |@GetMapping("/projects")
                    |fun getProjects() = projectService.getProjects(x.prop())
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc shouldn't trigger on actual methods`() {
        lintMethod(
            """
                    |actual fun writeToConsoleAc(msg: String, outputType: OutputStreamType) {}
                    |expect fun writeToConsoleEx(msg: String, outputType: OutputStreamType) {}
            """.trimMargin(),
            DiktatError(2, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} writeToConsoleEx", true),
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc shouldn't trigger on local functions`() {
        lintMethod(
            """
                |fun printHelloAndBye() {
                |    fun printHello() {
                |        print("Hello")
                |    }
                |    printHello()
                |    val ab = 5
                |    ab?.let {
                |        fun printBye() {
                |            print("Bye")
                |        }
                |        printBye()
                |    }
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} printHelloAndBye", false),
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc shouldn't trigger on functions with KDoc`() {
        lintMethod(
            """
                |/**
                | * prints "Hello" and "Bye"
                | */
                |fun printHelloAndBye() {
                |    fun printHello() {
                |        print("Hello")
                |    }
                |    printHello()
                |    val ab = 5
                |    ab?.let {
                |        fun printBye() {
                |            print("Bye")
                |        }
                |        printBye()
                |    }
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc shouldn't trigger on nested local functions`() {
        lintMethod(
            """
                |fun printHelloAndBye() {
                |    fun printHello() {
                |        print("Hello")
                |        fun printBye() {
                |            print("Bye")
                |        }
                |        fun printDots() {
                |            print("...")
                |        }
                |        printBye()
                |        printDots()
                |    }
                |    printHello()
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} printHelloAndBye", false),
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc shouldn't trigger on local functions with KDoc`() {
        lintMethod(
            """
                |fun printHelloAndBye() {
                |    fun printHello() {
                |        print("Hello")
                |    }
                |    printHello()
                |    val ab = 5
                |    ab?.let {
                |        /**
                |         * prints "Bye"
                |         */
                |        fun printBye() {
                |            print("Bye")
                |        }
                |        printBye()
                |    }
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} printHelloAndBye", false),
        )
    }

}
