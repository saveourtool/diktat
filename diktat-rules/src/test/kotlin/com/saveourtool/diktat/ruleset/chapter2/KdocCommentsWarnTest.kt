package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_DUPLICATE_PROPERTY
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_EXTRA_PROPERTY
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
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
                    |package com.saveourtool.diktat.example
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
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT), Tag(WarningNames.KDOC_EXTRA_PROPERTY))
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
            DiktatError(3, 4, ruleId, "${KDOC_EXTRA_PROPERTY.warnText()} @param adsf", false),
            DiktatError(7, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for property <name> to KDoc", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `should trigger on override parameter`() {
        lintMethod(
            """
                    |@Suppress("MISSING_KDOC_TOP_LEVEL")
                    |public class Example (
                    |   override val serializersModule: SerializersModule = EmptySerializersModule
                    |)
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add property <serializersModule> to KDoc", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
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
                    |   oneMoreName: String
                    |   )
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY))
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
                    |   oneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for property <name> to KDoc", true),
            DiktatError(7, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <anotherName> to KDoc", true),
            DiktatError(8, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <oneMoreName> to KDoc", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY))
    fun `check constructor with block comment`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /*some descriptions*/val name: String,
                    |   anotherName: String,
                    |   private val oneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for property <name> to KDoc", true),
            DiktatError(6, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <anotherName> to KDoc", true),
            DiktatError(7, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <oneMoreName> to KDoc", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY))
    fun `check not property but params`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   //some descriptions
                    |   private val name: String,
                    |   anotherName: String,
                    |   private val oneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for param <name> to KDoc", true),
            DiktatError(7, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <anotherName> to KDoc", true),
            DiktatError(8, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <oneMoreName> to KDoc", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY))
    fun `check constructor with kdoc`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /**
                    |    * some descriptions
                    |    */
                    |   val name: String,
                    |   anotherName: String,
                    |   private val oneMoreName: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for property <name> to KDoc", true),
            DiktatError(9, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <anotherName> to KDoc", true),
            DiktatError(10, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <oneMoreName> to KDoc", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `shouldn't fix because KDoc comment and property tag inside`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /**
                    |    * sdcjkh
                    |    * @property name text2
                    |    * fdfdfd
                    |    */
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for property <name> to KDoc", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `shouldn't fix because KDoc comment and any tag inside`() {
        lintMethod(
            """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /**
                    |    * sdcjkh
                    |    * @return name text2
                    |    * fdfdfd
                    |    */
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for property <name> to KDoc", false)
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.KDOC_EXTRA_PROPERTY))
    fun `no property kdoc`() {
        lintMethod(
            """
                    |/**
                    | * @property Name text
                    | */
                    |class Example (
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, "${KDOC_EXTRA_PROPERTY.warnText()} @property Name text", false),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add property <name> to KDoc", true)
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
                    |   val name: String
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${KDOC_EXTRA_PROPERTY.warnText()} @property kek", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `change property to param in kdoc for private parameter`() {
        lintMethod(
            """
                    |/**
                    | * @property name abc
                    | */
                    |class Example (
                    |   private val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} change `@property` tag to `@param` tag for <name> to KDoc", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `change param to property in kdoc for property`() {
        lintMethod(
            """
                    |/**
                    | * @param name abc
                    | */
                    |class Example (
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} change `@param` tag to `@property` tag for <name> to KDoc", true),
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `change param to property in kdoc for property with single comment`() {
        lintMethod(
            """
                    |/**
                    | * @param name abc
                    | */
                    |class Example (
                    |   //some descriptions
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} change `@param` tag to `@property` tag for <name> and add comment to KDoc", true),
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `change param to property in kdoc for property with block comment`() {
        lintMethod(
            """
                    |/**
                    | * @param name abc
                    | */
                    |class Example (
                    |   /*some descriptions*/
                    |   val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} change `@param` tag to `@property` tag for <name> and add comment to KDoc", true),
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `comment on private parameter`() {
        lintMethod(
            """
                    |/**
                    | * abc
                    | */
                    |class Example (
                    |   // single-line comment
                    |   private val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId,"${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} add comment for param <name> to KDoc", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `should trigger on private parameter`() {
        lintMethod(
            """
                    |/**
                    | * text
                    | */
                    |class Example (
                    |   private val name: String,
                    |   ) {
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId,"${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <name> to KDoc", true)
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
            DiktatError(2, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add property <name> to KDoc", true),
            DiktatError(3, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <surname> to KDoc", true)
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
                |class example {
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

    @Test
    @Tag(WarningNames.KDOC_EXTRA_PROPERTY)
    fun `shouldn't warn extra property on generic type`() {
        lintMethod(
            """
                |/**
                | * S3 implementation of Storage
                | *
                | * @param s3Operations [S3Operations] to operate with S3
                | * @param K type of key
                | */
                |abstract class AbstractReactiveStorage<K : Any> constructor(
                |    s3Operations: S3Operations,
                |) : ReactiveStorage<K> {
                |  // abcd
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `should trigger on generic type`() {
        lintMethod(
            """
                |/**
                | * S3 implementation of Storage
                | *
                | * @param s3Operations [S3Operations] to operate with S3
                | */
                |abstract class AbstractReactiveStorage<K : Any, P: Any>(
                |    s3Operations: S3Operations,
                |) : ReactiveStorage<K>, AnotherStorage<P> {
                |  // abcd
                |}
            """.trimMargin(),
            DiktatError(6, 40, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <K> to KDoc", true),
            DiktatError(6, 49, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <P> to KDoc", true),
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `change property to param in kdoc for generic type`() {
        lintMethod(
            """
                |/**
                | * S3 implementation of Storage
                | *
                | * @param s3Operations [S3Operations] to operate with S3
                | * @property K type of key
                | */
                |abstract class AbstractReactiveStorage<K : Any, P: Any>(
                |    s3Operations: S3Operations,
                |) : ReactiveStorage<K>, AnotherStorage<P> {
                |  // abcd
                |}
            """.trimMargin(),
            DiktatError(7, 40, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} change `@property` tag to `@param` tag for <K> to KDoc", true),
            DiktatError(7, 49, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add param <P> to KDoc", true),
        )
    }
}
