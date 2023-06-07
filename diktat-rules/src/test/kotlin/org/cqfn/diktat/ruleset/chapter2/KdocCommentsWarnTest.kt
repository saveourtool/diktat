package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_DUPLICATE_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_EXTRA_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import org.cqfn.diktat.util.LintTestBase

import org.cqfn.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class KdocCommentsWarnTest : LintTestBase(::KdocComments) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}"

    @Test
    @Tag(WarningNames.COMMENTED_BY_KDOC)
    fun `Should warn if kdoc comment is inside code block`() {
        val code =
            """
                    |package org.cqfn.diktat.example
                    |
                    |/**
                    |  * right place for kdoc
                    |  */
                    |class Example {
                    |/**
                    |  * right place for kdoc
                    |  */
                    |    fun doGood(){
                    |        /**
                    |         * wrong place for kdoc
                    |         */
                    |        1+2
                    |        /**
                    |         * right place for kdoc
                    |         */
                    |        fun prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
                    |            return "test"
                    |        }
                    |    }
                    |}
            """.trimMargin()
        lintMethod(
            code,
            DiktatError(
                11, 9, ruleId, "${Warnings.COMMENTED_BY_KDOC.warnText()} Redundant asterisk in block comment: \\**", true
            )
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all public classes should be documented with KDoc`() {
        val code =
            """
                class SomeGoodName {
                    private class InternalClass {
                    }
                }

                public open class SomeOtherGoodName {
                }

                open class SomeNewGoodName {
                }

                public class SomeOtherNewGoodName {
                }

            """.trimIndent()
        lintMethod(
            code,
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName"),
            DiktatError(6, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherGoodName"),
            DiktatError(9, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeNewGoodName"),
            DiktatError(12, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherNewGoodName")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all internal classes should be documented with KDoc`() {
        val code =
            """
                internal class SomeGoodName {
                }
            """.trimIndent()
        lintMethod(
            code, DiktatError(
                1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName"
            )
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all internal and public functions on top-level should be documented with Kdoc`() {
        val code =
            """
                fun someGoodName() {
                }

                internal fun someGoodNameNew(): String {
                    return " ";
                }

                fun main() {}
            """.trimIndent()
        lintMethod(
            code,
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodName"),
            DiktatError(4, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodNameNew")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all internal and public functions on top-level should be documented with Kdoc (positive case)`() {
        val code =
            """
                private fun someGoodName() {
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `positive Kdoc case with private class`() {
        val code =
            """
                private class SomeGoodName {
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc should present for each class element`() {
        val code =
            """
                /**
                * class that contains fields, functions and public subclasses
                **/
                class SomeGoodName {
                    val variable: String = ""
                    private val privateVariable: String = ""
                    fun perfectFunction() {
                    }

                    private fun privateFunction() {
                    }

                    class InternalClass {
                    }

                    private class InternalClass {
                    }

                    public fun main() {}
                }
            """.trimIndent()
        lintMethod(
            code,
            DiktatError(5, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} variable"),
            DiktatError(7, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} perfectFunction"),
            DiktatError(13, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} InternalClass")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc shouldn't not be mandatory for overridden functions and props`() {
        val code =
            """
                /**
                * class that contains fields, functions and public subclasses
                **/
                class SomeGoodName : Another {
                    val variable: String = ""
                    private val privateVariable: String = ""
                    override val someVal: String = ""
                    fun perfectFunction() {
                    }

                    override fun overrideFunction() {
                    }

                    class InternalClass {
                    }

                    private class InternalClass {
                    }

                    public fun main() {}
                }
            """.trimIndent()
        lintMethod(
            code,
            DiktatError(5, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} variable"),
            DiktatError(8, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} perfectFunction"),
            DiktatError(14, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} InternalClass")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc shouldn't present for each class element because Test annotation`() {
        lintMethod(
            """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    @Test
                    class SomeGoodName {
                        val variable: String = ""
                        private val privateVariable: String = ""
                        fun perfectFunction() {
                        }

                        private fun privateFunction() {
                        }

                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                    }
            """.trimIndent()
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc should present for each class element (positive)`() {
        val code =
            """
                /**
                * class that contains fields, functions and public subclasses
                **/
                class SomeGoodName {
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    val variable: String = ""

                    private val privateVariable: String = ""

                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    fun perfectFunction() {
                    }

                    private fun privateFunction() {
                    }

                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    class InternalClass {
                    }

                    private class InternalClass {
                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `regression - should not force documentation on standard methods`() {
        lintMethod(
            """
                    |/**
                    | * This is an example class
                    | */
                    |class Example {
                    |    override fun toString() = ""
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `check simple primary constructor with comment`() {
        lintMethod(
            """
                    |/**
                    | * @property name d
                    | * @param adsf
                    | * @return something
                    | */
                    |class Example constructor (
                    |   // short
                    |   val name: String
                    |) {
                    |}
            """.trimMargin(),
            DiktatError(7, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} name", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't trigger on override parameter`() {
        lintMethod(
            """
                    |@Suppress("MISSING_KDOC_TOP_LEVEL")
                    |public class Example (
                    |   override val serializersModule: SerializersModule = EmptySerializersModule
                    |)
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't trigger because not primary constructor`() {
        lintMethod(
            """
                    |/**
                    | * @property name d
                    | * @property anotherName text
                    | */
                    |class Example {
                    |   constructor(
                    |   // name
                    |   name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   )
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `check constructor with comment`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   //some descriptions
                    |   val name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} name", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check constructor with block comment`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /*some descriptions*/val name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} /*some descriptions*/", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check not property`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   //some descriptions
                    |   name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check constructor with kdoc`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /**
                    |    * some descriptions
                    |    * @return fdv
                    |    */
                    |
                    |   val name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} /**...", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't fix`() {
        lintMethod(
            """
                    |/**
                    | * @property name text
                    | */
                    |class Example (
                    |   /**
                    |    * sdcjkh
                    |    * @property name text2
                    |    */
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} /**...", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't trigger`() {
        lintMethod(
            """
                    |/**
                    | * text
                    | */
                    |class Example (
                    |   private val name: String,
                    |   ) {
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.KDOC_EXTRA_PROPERTY))
    fun `no property kdoc`() {
        lintMethod(
            """
                    |/**
                    | * @property Name text
                    | * @property
                    | */
                    |class Example (
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, "${KDOC_EXTRA_PROPERTY.warnText()} @property Name text", false),
            DiktatError(6, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add <name> to KDoc", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.MISSING_KDOC_TOP_LEVEL))
    fun `no property kdoc and class`() {
        lintMethod(
            """
                    |class Example (
                    |   val name: String,
                    |   private val surname: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} Example"),
            DiktatError(2, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add <name> to KDoc", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_EXTRA_PROPERTY)
    fun `extra property in kdoc`() {
        lintMethod(
            """
                    |/**
                    | * @property name bla
                    | * @property kek
                    | */
                    |class Example (
                    |   val name: String,
                    |   private val surname: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${KDOC_EXTRA_PROPERTY.warnText()} @property kek", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER)
    fun `property described only in class KDoc`() {
        lintMethod(
            """
                |/**
                | * @property foo lorem ipsum
                | */
                |class Example {
                |    val foo: Any
                |}
            """.trimMargin(),
            DiktatError(5, 5, ruleId, "${KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER.warnText()} val foo: Any")
        )
    }

    @Test
    fun `property described both in class KDoc and own KDoc`() {
        lintMethod(
            """
                |/**
                | * @property foo lorem ipsum
                | */
                |class Example {
                |    /**
                |     * dolor sit amet
                |     */
                |    val foo: Any
                |}
            """.trimMargin(),
            DiktatError(5, 5, ruleId, "${KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER.warnText()} /**...")
        )
    }

    @Test
    fun `shouldn't trigger kdoc top level on actual methods`() {
        lintMethod(
            """
                |actual fun foo() {}
                |expect fun fo() {}
                |internal actual fun foo() {}
                |internal expect fun foo() {}
                |
                |expect class B{}
                |
                |actual class A{}
            """.trimMargin(),
            DiktatError(2, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} fo"),
            DiktatError(4, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} foo"),
            DiktatError(6, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} B")
        )
    }

    @Test
    fun `should find Kdoc after annotation of function`() {
        lintMethod(
            """
                |@SomeAnnotation
                |/**
                | * Just print a string
                | *
                | * @param f string to print
                | * @return 1
                | */
                |internal fun prnt(f: String) {
                |   println(f)
                |   return 1
                |}
            """.trimMargin()
        )
    }

    @Test
    fun `should find Kdoc after annotation of class`() {
        lintMethod(
            """
                |@SomeAnnotation
                |/**
                | * Test class
                | */
                |class example(f: String) {
                |
                |}
            """.trimMargin()
        )
    }

    @Test
    fun `should find Kdoc inside a modifier list`() {
        lintMethod(
            """
                |public
                |/**
                | * foo
                | */
                |actual fun foo() { }
            """.trimMargin()
        )
    }

    @Test
    fun `should warn if there are duplicate tags 1`() {
        lintMethod(
            """
                |/**
                | * @param field1 description1
                | * @param field2 description2
                | * @param field2
                | */
                |fun foo(field1: Long, field2: Int) {
                |    //
                |}
            """.trimMargin(),
            DiktatError(4, 4, ruleId, "${KDOC_DUPLICATE_PROPERTY.warnText()} @param field2"),
        )
    }

    @Test
    fun `should warn if there are duplicate tags 2`() {
        lintMethod(
            """
                |/**
                | * @property field1
                | * @property field2
                | * @property field2
                | */
                |@Serializable
                |data class DataClass(
                |    val field1: String,
                |    val field2: String,
                |)
            """.trimMargin(),
            DiktatError(4, 4, ruleId, "${KDOC_DUPLICATE_PROPERTY.warnText()} @property field2"),
        )
    }

    @Test
    fun `should warn if there are duplicate tags 3`() {
        lintMethod(
            """
                |/**
                | * @property field1
                | * @property field2
                | * @param field2
                | */
                |@Serializable
                |data class DataClass(
                |    val field1: String,
                |    val field2: String,
                |)
            """.trimMargin(),
            DiktatError(4, 4, ruleId, "${KDOC_DUPLICATE_PROPERTY.warnText()} @param field2"),
        )
    }

    @Test
    fun `should warn if there are duplicate tags 4`() {
        lintMethod(
            """
                |/**
                | * @property field1
                | * @property field1
                | * @property field2
                | * @param field2
                | */
                |@Serializable
                |data class DataClass(
                |    val field1: String,
                |    val field2: String,
                |)
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${KDOC_DUPLICATE_PROPERTY.warnText()} @property field1"),
            DiktatError(5, 4, ruleId, "${KDOC_DUPLICATE_PROPERTY.warnText()} @param field2"),
        )
    }
}
