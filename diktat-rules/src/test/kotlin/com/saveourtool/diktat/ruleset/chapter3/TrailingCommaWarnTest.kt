package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TRAILING_COMMA
import com.saveourtool.diktat.ruleset.rules.chapter3.TrailingCommaRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TrailingCommaWarnTest : LintTestBase(::TrailingCommaRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${TrailingCommaRule.NAME_ID}"

    private fun getRulesConfig(paramName: String): List<RulesConfig> = listOf(
        RulesConfig(
            TRAILING_COMMA.name, true,
            mapOf(paramName to "true"))
    )

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check value arguments`() {
        lintMethod(
            """
                fun shift(x: Int, y: Int) {
                    shift(
                        25,
                        20 // trailing comma
                    )

                    val colors = listOf(
                        "red",
                        "green",
                        "blue" // trailing comma
                    )
                }
            """.trimMargin(),
            DiktatError(4, 25, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_ARGUMENT: 20", true),
            DiktatError(10, 25, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_ARGUMENT: \"blue\"", true),
            rulesConfigList = getRulesConfig("valueArgument")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check class properties and parameters`() {
        lintMethod(
            """
                class Customer(
                    val name: String,
                    val lastName: String // trailing comma
                )

                class Customer(
                    val name: String,
                    lastName: String // trailing comma
                )
            """.trimMargin(),
            DiktatError(3, 21, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_PARAMETER: val lastName: String // trailing comma", true),
            DiktatError(8, 21, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_PARAMETER: lastName: String // trailing comma", true),
            rulesConfigList = getRulesConfig("valueParameter")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check function value parameters`() {
        lintMethod(
            """
                class A {

                    fun foo() {}

                    fun powerOf(
                        number: Int,
                        exponent: Int, // trailing comma
                    ) { /*...*/ }

                    constructor(
                        x: Comparable<Number>,
                        y: Iterable<Number>
                    ) {}

                    fun print(
                        vararg quantity: Int,
                        description: String
                    ) {}
                }
            """.trimMargin(),
            DiktatError(12, 25, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_PARAMETER: y: Iterable<Number>", true),
            DiktatError(17, 25, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_PARAMETER: description: String", true),
            rulesConfigList = getRulesConfig("valueParameter")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check parameters with optional type`() {
        lintMethod(
            """
                fun foo() {
                    val sum: (Int, Int, Int,) -> Int = fun(
                        x,
                        y,
                        z // trailing comma
                    ): Int {
                        return x + y + x
                    }
                    println(sum(8, 8, 8))
                }
            """.trimMargin(),
            DiktatError(5, 25, ruleId, "${TRAILING_COMMA.warnText()} after VALUE_PARAMETER: z // trailing comma", true),
            rulesConfigList = getRulesConfig("valueParameter")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check indexing suffix`() {
        lintMethod(
            """
                class Surface {
                    operator fun get(x: Int, y: Int) = 2 * x + 4 * y - 10
                }
                fun getZValue(mySurface: Surface, xValue: Int, yValue: Int) =
                    mySurface[
                        xValue,
                        yValue // trailing comma
                    ]
            """.trimMargin(),
            DiktatError(7, 25, ruleId, "${TRAILING_COMMA.warnText()} after REFERENCE_EXPRESSION: yValue", true),
            rulesConfigList = getRulesConfig("referenceExpression")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check lambda parameters`() {
        lintMethod(
            """
                fun main() {
                    val x = {
                            x: Comparable<Number>,
                            y: Iterable<Number>
                            -> println("1",)
                    }

                    println(x,)
                }
            """.trimMargin(),
            rulesConfigList = getRulesConfig("valueParameter")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check when entry`() {
        lintMethod(
            """
                fun isReferenceApplicable(myReference: KClass<*>) = when (myReference) {
                    Comparable::class,
                    Iterable::class,
                    String::class // trailing comma
                        -> true
                    else -> false
                }

                fun someFun() {
                   when (x) {
                       is Int,
                       is String
                            -> print((x as Int).length)
                       is Long, -> x as Int
                   }
               }

               fun someFun() {
                   when (x) {
                       in 1..2
                        -> foo()
                   }
               }

               fun someFun() {
                   when (x) {}
               }
            """.trimMargin(),
            DiktatError(4, 21, ruleId, "${TRAILING_COMMA.warnText()} after WHEN_CONDITION_WITH_EXPRESSION: String::class", true),
            DiktatError(12, 24, ruleId, "${TRAILING_COMMA.warnText()} after WHEN_CONDITION_IS_PATTERN: is String", true),
            DiktatError(20, 24, ruleId, "${TRAILING_COMMA.warnText()} after WHEN_CONDITION_IN_RANGE: in 1..2", true),
            rulesConfigList = getRulesConfig("whenConditions")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check collection literals`() {
        lintMethod(
            """
                annotation class ApplicableFor(val services: Array<String>)

                @ApplicableFor([
                    "serializer",
                    "balancer",
                    "database",
                    "inMemoryCache" // trailing comma
                ],)
                fun foo() {}
            """.trimMargin(),
            DiktatError(7, 21, ruleId, "${TRAILING_COMMA.warnText()} after STRING_TEMPLATE: \"inMemoryCache\"", true),
            rulesConfigList = getRulesConfig("collectionLiteral")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check type arguments`() {
        lintMethod(
            """
                fun <T1, T21,> foo() {}

                fun main() {
                    foo<
                            Comparable<Number,>,
                            Iterable<Number
                            > // trailing comma
                            >()
                }
            """.trimMargin(),
            DiktatError(6, 29, ruleId, "${TRAILING_COMMA.warnText()} after TYPE_PROJECTION: Iterable<Number...", true),
            DiktatError(6, 38, ruleId, "${TRAILING_COMMA.warnText()} after TYPE_PROJECTION: Number", true),
            rulesConfigList = getRulesConfig("typeArgument")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check type parameters`() {
        lintMethod(
            """
                class MyMap<
                        MyKey,
                        MyValue // trailing comma
                        > {}
            """.trimMargin(),
            DiktatError(3, 25, ruleId, "${TRAILING_COMMA.warnText()} after TYPE_PARAMETER: MyValue", true),
            rulesConfigList = getRulesConfig("typeParameter")
        )
    }

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `check destructuring declarations`() {
        lintMethod(
            """
                fun foo() {
                    data class Car(val manufacturer: String, val model: String, val year: Int)
                    val myCar = Car("Tesla", "Y", 2019)

                    val (
                        manufacturer,
                        model,
                        year // trailing comma
                    ) = myCar

                    val cars = listOf<Car>()
                    fun printMeanValue() {
                        var meanValue: Int = 0
                        for ((
                            _,
                            _,
                            year // trailing comma
                        ) in cars) {
                            meanValue += year
                        }
                        println(meanValue/cars.size)
                    }
                    printMeanValue()
                }
            """.trimMargin(),
            DiktatError(8, 25, ruleId, "${TRAILING_COMMA.warnText()} after DESTRUCTURING_DECLARATION_ENTRY: year", true),
            DiktatError(17, 29, ruleId, "${TRAILING_COMMA.warnText()} after DESTRUCTURING_DECLARATION_ENTRY: year", true),
            rulesConfigList = getRulesConfig("destructuringDeclaration")
        )
    }
}
